# BizCal Native Android Project

This project includes a GitHub Actions workflow that builds a debug APK automatically.

## Set it up using an Android phone

1. Download this ZIP and extract it. Do not upload the ZIP as one file.
2. Create a GitHub repository (for example, `BizCal`).
3. Upload the **contents** of the extracted folder to the repository root, preserving hidden folders and paths. In particular, `.github/workflows/build-apk.yml` must exist in the repository.
4. Make sure the files are committed to the `main` branch. A push to `main` starts the build automatically.
5. Open the repository's **Actions** tab and select **Build BizCal APK**. If needed, open the workflow and tap **Run workflow**.
6. When the run has a green check mark, open that run, scroll to **Artifacts**, and download **BizCal-APK**. The downloaded artifact is a ZIP containing `app-debug.apk`. Extract it and install the APK. Android may ask you to allow installs from that browser/file manager.

## If the build fails

Open the failed run in Actions, select the build step, and inspect the first Kotlin or Gradle error. A successful workflow uploads the APK as an artifact.

## Notes

- This is a debug APK, suitable for testing, not a signed Play Store release.
- BizCal stores its records locally on the device. Back up important information.
- Keep the repository private if you do not want the source code public.
