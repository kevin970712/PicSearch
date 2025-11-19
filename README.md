# PicSearch 🔍

use google reverse image search to search the image on your device.


## About this project

PicSearch is a super lightweight utility that hooks into Android Share Sheet. Let you quickly find the source of an image using Google Lens.

*   **Works from anywhere:** Use it from your photo gallery, web browser, or any app that can share images or links.
*   **Share an image:** Share a picture file directly from your phone.
*   **Share a link:** Share a URL that points to an image.

## How It Works

When you share an image file, Google Lens needs a public URL to analyze it. PicSearch makes this happen seamlessly:

1.  The app receives the shared image.
2.  It uploads the image to the temporary file hosting service **[tmpfiles.org](https://tmpfiles.org/)**.
3.  `tmpfiles.org` provides a direct, temporary link to that image.
4.  The app then hands this link over to Google Lens for the reverse image search.


 App Screenshot | Demo Video |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/93fd3cf8-20dc-404b-b6f5-73dd02bad59d"/> | ![App Demo](https://github.com/user-attachments/assets/d5ffbc8c-8e77-49ad-a20c-5b992a15a24e) |


## Built With 🛠️

This app was built using a modern, minimal Android tech stack:

*   **Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Networking:** Retrofit & OkHttp
*   **JSON Parsing:** Kotlinx Serialization
*   **Architecture:** Android ViewModel for state management
*   **Asynchronous Tasks:** Kotlin Coroutines

## Getting Started

To build this project yourself:

1.  Clone this repository.
2.  Open it in the latest version of Android Studio.
3.  Build and run!

## License

Licensed under the MIT License.
