
package com.bizcal.app

import android.app.*
import android.os.Bundle
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NAVY = 0xFF102A43.toInt()
private const val TEAL = 0xFF0D9488.toInt()
private const val BG = 0xFFF3F6FA.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()
private const val INK = 0xFF243B53.toInt()

class BizDb(ctx: Context) : SQLiteOpenHelper(ctx, "bizcal.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, cost REAL NOT NULL DEFAULT 0, price REAL NOT NULL DEFAULT 0, stock REAL NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT, customer TEXT, item TEXT NOT NULL, qty REAL NOT NULL, price REAL NOT NULL, total REAL NOT NULL, paid REAL NOT NULL, date TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun addProduct(name: String, cost: Double, price: Double, stock: Double) {
        writableDatabase.insert("products", null, ContentValues().apply {
            put("name", name)
            put("cost", cost)
            put("price", price)
            put("stock", stock)
        })
    }

    fun addCustomer(name: String, phone: String) {
        writableDatabase.insert("customers", null, ContentValues().apply {
            put("name", name)
            put("phone", phone)
        })
    }

    fun addSale(customer: String, item: String, qty: Double, price: Double, paid: Double) {
        writableDatabase.insert("sales", null, ContentValues().apply {
            put("customer", customer)
            put("item", item)
            put("qty", qty)
            put("price", price)
            put("total", qty * price)
            put("paid", paid)
            put("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
        })
    }

    fun count(table: String): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $table", null).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    fun sum(column: String): Double =
        readableDatabase.rawQuery("SELECT COALESCE(SUM($column),0) FROM sales", null).use {
            if (it.moveToFirst()) it.getDouble(0) else 0.0
        }

    fun rows(table: String): List<Array<String>> {
        val sql = when (table) {
            "products" -> "SELECT name,cost,price,stock FROM products ORDER BY id DESC"
            "customers" -> "SELECT name,phone FROM customers ORDER BY id DESC"
            else -> "SELECT customer,item,qty,price,total,paid,date FROM sales ORDER BY id DESC"
        }

        return readableDatabase.rawQuery(sql, null).use { c ->
            val list = mutableListOf<Array<String>>()
            while (c.moveToNext()) {
                list.add(Array(c.columnCount) { i -> c.getString(i) ?: "" })
            }
            list
        }
    }
}

class MainActivity : Activity() {

    private lateinit var db: BizDb
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var bengali = true
    private var current = "home"

    private fun t(bn: String, en: String) = if (bengali) bn else en

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = BizDb(this)
        show("home")
    }

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun text(
        s: String,
        size: Float = 15f,
        color: Int = INK,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(2), dp(5), dp(2), dp(5))
    }

    private fun button(s: String, on: () -> Unit): Button =
        Button(this).apply {
            text = s
            isAllCaps = false
            setTextColor(WHITE)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(TEAL)
            setOnClickListener { on() }
        }

    private fun field(hint: String): EditText =
        EditText(this).apply {
            this.hint = hint
            textSize = 15f
            isSingleLine = true
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(WHITE)
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), 0xFFD8E1EA.toInt())
            }
        }

    private fun addSpace(parent: LinearLayout, h: Int = 8) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(h)))
    }

    private fun card(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(WHITE)
                cornerRadius = dp(18).toFloat()
            }
        }

    private fun show(page: String) {
        current = page

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            setBackgroundColor(NAVY)
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        top.addView(
            text("BizCal", 25f, WHITE, true),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        top.addView(Button(this).apply {
            text = if (bengali) "বাংলা" else "English"
            isAllCaps = false
            setOnClickListener {
                bengali = !bengali
                show(current)
            }
        })

        header.addView(top)
        header.addView(
            text(
                t("আপনার ব্যবসার হিসাব, সহজভাবে", "Your business, calculated simply"),
                13f,
                0xFFD9E7F2.toInt()
            )
        )

        root.addView(header)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(18))
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(5), dp(5), dp(5), dp(5))
            setBackgroundColor(WHITE)
        }

        listOf(
            "home" to t("হোম", "Home"),
            "products" to t("পণ্য", "Products"),
            "customers" to t("ক্রেতা", "Customers"),
            "sales" to t("বিক্রি", "Sales")
        ).forEach { (id, label) ->
            nav.addView(
                Button(this).apply {
                    text = label
                    isAllCaps = false
                    textSize = 11f
                    setOnClickListener { show(id) }
                },
                LinearLayout.LayoutParams(0, dp(52), 1f)
            )
        }

        root.addView(nav)
        setContentView(root)

        when (page) {
            "home" -> home()
            "products" -> products()
            "customers" -> customers()
            "sales" -> sales()
            "newProduct" -> newProduct()
            "newCustomer" -> newCustomer()
            "newSale" -> newSale()
            "history" -> history()
        }
    }

    private fun title(s: String) {
        content.addView(text(s, 21f, NAVY, true))
        addSpace(content, 6)
    }

    private fun metric(label: String, value: String): LinearLayout {
        val c = card()
        c.addView(text(label, 13f, 0xFF627D98.toInt()))
        c.addView(text(value, 24f, NAVY, true))
        return c
    }

    private fun home() {
        title(t("ব্যবসার সারাংশ", "Business overview"))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            metric(t("মোট বিক্রি", "Total sales"), "৳ ${fmt(db.sum("total"))}"),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        row.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))

        row.addView(
            metric(t("বকেয়া", "Due amount"), "৳ ${fmt(db.sum("total") - db.sum("paid"))}"),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        content.addView(row)

        addSpace(content, 10)

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row2.addView(
            metric(t("পণ্য", "Products"), db.count("products").toString()),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        row2.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))

        row2.addView(
            metric(t("ক্রেতা", "Customers"), db.count("customers").toString()),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        content.addView(row2)

        addSpace(content, 18)
        title(t("দ্রুত কাজ", "Quick actions"))

        content.addView(button(t("＋ নতুন বিক্রির স্লিপ", "＋ New sales slip")) {
            show("newSale")
        })
        addSpace(content, 8)

        content.addView(button(t("＋ পণ্য যোগ করুন", "＋ Add product")) {
            show("newProduct")
        })
        addSpace(content, 8)

        content.addView(button(t("＋ ক্রেতা যোগ করুন", "＋ Add customer")) {
            show("newCustomer")
        })
        addSpace(content, 8)

        content.addView(button(t("স্লিপের ইতিহাস ও PDF", "Slip history & PDF")) {
            show("history")
        })
        addSpace(content, 14)

        content.addView(
            text(
                t(
                    "ডেটা এই ফোনেই অফলাইনে সংরক্ষিত হয়। নিয়মিত ব্যাকআপ রাখুন।",
                    "Your data is stored offline on this phone. Keep regular backups."
                ),
                12f,
                0xFF627D98.toInt()
            )
        )
    }

    private fun products() {
        title(t("পণ্য তালিকা", "Products"))
        content.addView(button(t("＋ পণ্য যোগ করুন", "＋ Add product")) {
            show("newProduct")
        })
        addSpace(content, 10)

        val rows = db.rows("products")

        if (rows.isEmpty()) {
            content.addView(text(t("এখনও কোনো পণ্য যোগ করা হয়নি।", "No products added yet.")))
        } else {
            rows.forEach { r ->
                val c = card()
                c.addView(text(r[0], 17f, NAVY, true))
                c.addView(
                    text(
                        "${t("ক্রয়", "Cost")}: ৳${r[1]}  •  " +
                            "${t("বিক্রি", "Price")}: ৳${r[2]}  •  " +
                            "${t("স্টক", "Stock")}: ${r[3]}"
                    )
                )
                addSpace(content, 8)
                content.addView(c)
            }
        }
    }

    private fun customers() {
        title(t("ক্রেতা তালিকা", "Customers"))
        content.addView(button(t("＋ ক্রেতা যোগ করুন", "＋ Add customer")) {
            show("newCustomer")
        })
        addSpace(content, 10)

        val rows = db.rows("customers")

        if (rows.isEmpty()) {
            content.addView(text(t("এখনও কোনো ক্রেতা যোগ করা হয়নি।", "No customers added yet.")))
        } else {
            rows.forEach { r ->
                val c = card()
                c.addView(text(r[0], 17f, NAVY, true))
                c.addView(text(r.getOrElse(1) { "" }))
                addSpace(content, 8)
                content.addView(c)
            }
        }
    }

    private fun sales() {
        title(t("বিক্রির হিসাব", "Sales"))

        content.addView(button(t("＋ নতুন স্লিপ", "＋ New slip")) {
            show("newSale")
        })
        addSpace(content, 8)

        content.addView(button(t("স্লিপের ইতিহাস", "Slip history")) {
            show("history")
        })
        addSpace(content, 12)

        val rows = db.rows("sales")

        if (rows.isEmpty()) {
            content.addView(text(t("এখনও বিক্রির রেকর্ড নেই।", "No sales recorded yet.")))
        } else {
            rows.take(10).forEach { r ->
                val c = card()
                c.addView(text("${r[1]}  •  ৳${r[4]}", 16f, NAVY, true))
                c.addView(
                    text("${r[0]} | ${r[6]} | ${t("পরিশোধ", "Paid")}: ৳${r[5]}")
                )
                addSpace(content, 8)
                content.addView(c)
            }
        }
    }

    private fun formField(label: String, hint: String): Pair<LinearLayout, EditText> {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        wrap.addView(text(label, 13f, INK, true))
        val e = field(hint)
        wrap.addView(e)
        addSpace(wrap, 8)
        return wrap to e
    }

    private fun newProduct() {
        title(t("নতুন পণ্য", "New product"))

        val (a, n) = formField(
            t("পণ্যের নাম *", "Product name *"),
            t("যেমন: Cotton T-shirt", "e.g. Cotton T-shirt")
        )
        content.addView(a)

        val (b, c) = formField(t("প্রতি ইউনিট ক্রয়মূল্য", "Unit cost"), "0")
        content.addView(b)

        val (d, p) = formField(t("প্রতি ইউনিট বিক্রয়মূল্য", "Unit selling price"), "0")
        content.addView(d)

        val (e, s) = formField(t("স্টকের পরিমাণ", "Stock quantity"), "0")
        content.addView(e)

        content.addView(button(t("পণ্য সংরক্ষণ করুন", "Save product")) {
            if (n.text.toString().trim().isEmpty()) {
                n.error = t("নাম লিখুন", "Enter a name")
                return@button
            }

            db.addProduct(
                n.text.toString().trim(),
                c.num(),
                p.num(),
                s.num()
            )

            Toast.makeText(
                this,
                t("পণ্য সংরক্ষিত হয়েছে", "Product saved"),
                Toast.LENGTH_SHORT
            ).show()

            show("products")
        })
    }

    private fun newCustomer() {
        title(t("নতুন ক্রেতা", "New customer"))

        val (a, n) = formField(t("ক্রেতার নাম *", "Customer name *"), t("নাম লিখুন", "Enter name"))
        content.addView(a)

        val (b, p) = formField(t("ফোন নম্বর", "Phone number"), "01...")
        content.addView(b)

        content.addView(button(t("ক্রেতা সংরক্ষণ করুন", "Save customer")) {
            if (n.text.toString().trim().isEmpty()) {
                n.error = t("নাম লিখুন", "Enter a name")
                return@button
            }

            db.addCustomer(n.text.toString().trim(), p.text.toString().trim())

            Toast.makeText(
                this,
                t("ক্রেতা সংরক্ষিত হয়েছে", "Customer saved"),
                Toast.LENGTH_SHORT
            ).show()

            show("customers")
        })
    }

    private fun newSale() {
        title(t("নতুন বিক্রির স্লিপ", "New sales slip"))

        val (a, cust) = formField(t("ক্রেতার নাম", "Customer name"), t("ঐচ্ছিক", "Optional"))
        content.addView(a)

        val (b, item) = formField(t("পণ্য/সেবার নাম *", "Product/service *"), t("পণ্যের নাম", "Item name"))
        content.addView(b)

        val (c, qty) = formField(t("পরিমাণ", "Quantity"), "1")
        content.addView(c)

        val (d, price) = formField(t("প্রতি ইউনিট মূল্য (৳)", "Unit price (৳)"), "0")
        content.addView(d)

        val (e, paid) = formField(t("এখন পরিশোধ (৳)", "Paid now (৳)"), "0")
        content.addView(e)

        val total = text(t("মোট: ৳0.00", "Total: ৳0.00"), 19f, TEAL, true)
        content.addView(total)

        val update = {
            total.text = "${t("মোট", "Total")}: ৳${fmt(qty.num() * price.num())}"
        }

        qty.setOnFocusChangeListener { _, _ -> update() }
        price.setOnFocusChangeListener { _, _ -> update() }

        content.addView(button(t("স্লিপ সংরক্ষণ করুন", "Save slip")) {
            if (item.text.toString().trim().isEmpty()) {
                item.error = t("পণ্যের নাম লিখুন", "Enter item name")
                return@button
            }

            if (qty.num() <= 0 || price.num() < 0 || paid.num() < 0) {
                Toast.makeText(
                    this,
                    t("পরিমাণ ও মূল্য ঠিক করুন", "Check quantity and price"),
                    Toast.LENGTH_SHORT
                ).show()
                return@button
            }

            db.addSale(
                cust.text.toString().trim(),
                item.text.toString().trim(),
                qty.num(),
                price.num(),
                paid.num()
            )

            val sale = Sale(
                cust.text.toString(),
                item.text.toString(),
                qty.num(),
                price.num(),
                qty.num() * price.num(),
                paid.num(),
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            )

            createPdf(sale)

            Toast.makeText(
                this,
                t("স্লিপ সংরক্ষিত। PDF ফাইল তৈরি হয়েছে।", "Slip saved. PDF created."),
                Toast.LENGTH_LONG
            ).show()

            show("history")
        })
    }

    private data class Sale(
        val customer: String,
        val item: String,
        val qty: Double,
        val price: Double,
        val total: Double,
        val paid: Double,
        val date: String
    )

    private fun createPdf(s: Sale) {
        val doc = PdfDocument()
        val page = doc.startPage(
            PdfDocument.PageInfo.Builder(595, 842, 1).create()
        )

        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = NAVY
        canvas.drawRect(0f, 0f, 595f, 120f, paint)

        paint.color = WHITE
        paint.textSize = 30f
        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        canvas.drawText("BizCal", 40f, 55f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        canvas.drawText(
            if (bengali) "বিক্রির স্লিপ / INVOICE" else "SALES SLIP / INVOICE",
            40f, 85f, paint
        )

        paint.color = INK
        paint.textSize = 14f
        canvas.drawText("Date: ${s.date}", 40f, 155f, paint)
        canvas.drawText(
            "Customer: ${s.customer.ifBlank { "Walk-in customer" }}",
            40f, 185f, paint
        )

        paint.color = TEAL
        canvas.drawRect(40f, 220f, 555f, 255f, paint)

        paint.color = WHITE
        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        canvas.drawText("Item", 52f, 242f, paint)
        canvas.drawText("Qty", 300f, 242f, paint)
        canvas.drawText("Price", 370f, 242f, paint)
        canvas.drawText("Total", 465f, 242f, paint)

        paint.color = INK
        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        canvas.drawText(s.item.take(30), 52f, 285f, paint)
        canvas.drawText(fmt(s.qty), 300f, 285f, paint)
        canvas.drawText(fmt(s.price), 370f, 285f, paint)
        canvas.drawText(fmt(s.total), 465f, 285f, paint)

        paint.color = 0xFFD8E1EA.toInt()
        canvas.drawLine(40f, 310f, 555f, 310f, paint)

        paint.color = INK
        paint.textSize = 16f
        canvas.drawText("Total: BDT ${fmt(s.total)}", 350f, 355f, paint)
        canvas.drawText("Paid: BDT ${fmt(s.paid)}", 350f, 385f, paint)

        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        canvas.drawText("Due: BDT ${fmt(s.total - s.paid)}", 350f, 415f, paint)

        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paint.textSize = 12f
        paint.color = 0xFF627D98.toInt()
        canvas.drawText(
            "Created with BizCal • Business Calculator & Slip Maker",
            40f, 790f, paint
        )

        doc.finishPage(page)

        val dir = File(getExternalFilesDir(null), "BizCal")
        dir.mkdirs()

        val file = File(dir, "BizCal-${System.currentTimeMillis()}.pdf")

        try {
            FileOutputStream(file).use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
    }

    private fun history() {
        title(t("স্লিপের ইতিহাস", "Slip history"))

        content.addView(
            text(
                t(
                    "PDF ফাইল এই অ্যাপের ফোল্ডারে সেভ হয়: Android/data/com.bizcal.app/files/BizCal",
                    "PDFs are saved in this app folder: Android/data/com.bizcal.app/files/BizCal"
                ),
                12f,
                0xFF627D98.toInt()
            )
        )

        addSpace(content, 8)

        val rows = db.rows("sales")

        if (rows.isEmpty()) {
            content.addView(text(t("এখনও কোনো স্লিপ নেই।", "No slips yet.")))
        } else {
            rows.forEach { r ->
                val c = card()
                c.addView(text(r[1], 16f, NAVY, true))
                c.addView(
                    text(
                        "${r[6]} • ৳${r[4]} • ${t("বকেয়া", "Due")}: ৳${
                            fmt(
                                r[4].toDoubleOrNull().orZero() -
                                    r[5].toDoubleOrNull().orZero()
                            )
                        }"
                    )
                )
                addSpace(content, 8)
                content.addView(c)
            }
        }
    }

    private fun Double?.orZero() = this ?: 0.0

    private fun EditText.num() =
        text.toString().trim().toDoubleOrNull() ?: 0.0

    private fun fmt(n: Double) =
        String.format(Locale.US, "%,.2f", n)
}
