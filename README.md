# TikTok Video Downloader (Android)

👉 **[Veja este README em Português 🇧🇷](README.pt-BR.md)**

A modern, native Android application capable of downloading TikTok videos without watermarks. This project demonstrates advanced Android development skills, focusing on **Modern Android Architecture**, **Jetpack Compose**, and **UI/UX Design**.

## 🚀 Features

- **Watermark-Free Downloads:** Fetches and downloads clean MP4 video files directly to the device.
- **Modern UI/UX:** Custom-built interface featuring **Glassmorphism**, ambient glow effects, and TikTok-inspired gradients.
- **Reactive State Management:** Real-time UI updates using `StateFlow` and generic UI States (Loading, Success, Error).
- **Smart Clipboard Integration:** Automatically detects copied links from TikTok.
- **Visual Feedback:** Animated transitions and loading indicators.

## 📸 Preview

<p align="center">
  <img src="img/preview.jpeg" alt="Preview" width="350" style="max-width:100%; height:auto;">
</p>


## 🛠 Tech Stack & Architecture

This project follows the recommended **MVVM (Model-View-ViewModel)** architecture ensuring separation of concerns and testability.

- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM + Unidirectional Data Flow (UDF)
- **Asynchronous Processing:** Coroutines & Flow
- **Networking:** Retrofit 2 + Gson
- **Image Loading:** Coil
- **System Integration:** Android DownloadManager API

## 📂 Project Structure

```
com.naicolasdev.tiktokdownloader
├── ui
│   ├── components   # Reusable UI elements (GlassPanel, GradientButton)
│   ├── home         # Main Screen & Result Cards
│   └── theme        # Custom Design System (Colors, Type, Shapes)
├── data
│   └── api          # Retrofit Service & Data Models
├── viewmodel
│   └── MainViewModel.kt  # State management logic
└── MainActivity.kt       # Entry point
```

## 🔧 How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/naicolas-dev/tiktok-downloader.git
   ```

2. Open in Android Studio and wait for Gradle to sync.

3. Run the app on a physical device or emulator.

## 📝 License & Disclaimer

This project is for educational and portfolio purposes only.  
It acts as a client for publicly available APIs and is not affiliated with, endorsed by, or connected to TikTok or ByteDance.

Please respect the intellectual property rights of content creators and use this tool responsibly.

---

Developed by **Nicolas Viana Alves**


