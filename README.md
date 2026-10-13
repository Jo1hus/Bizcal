# BizCal Native Android MVP

Native Kotlin Android app (not a WebView wrapper), bilingual Bangla/English UI, offline SQLite storage, basic product/customer/sales records, and PDF invoice generation.

## Build without a computer
1. Sign in to GitHub in your phone browser and create a new **private** repository named `BizCal`.
2. Upload all files and folders in this project to the repository, keeping paths and the `.github/workflows/build-apk.yml` workflow intact. Easiest route: use GitHub's web UI to upload the ZIP contents after extracting it, or the GitHub mobile website's file upload.
3. Open the repository's **Actions** tab, select **Build BizCal APK**, and tap **Run workflow** (or push to `main`).
4. When it completes, open the run and download the `BizCal-debug-apk` artifact. Extract the downloaded ZIP and install `app-debug.apk`; Android may ask permission to install unknown apps.

## Important MVP limits
- This is a starter build, not yet a production-ready accounting package.
- PDF files are saved in the app's private external-files folder. Share/export UI, database backup/restore, edit/delete records, stock deduction, advanced invoice settings and proper testing are still to be added.
- Keep the repository private if you do not want the source public. Do not upload private customer data into the repository.
