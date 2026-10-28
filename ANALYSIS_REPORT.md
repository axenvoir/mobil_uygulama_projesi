# Codebase Analysis Report

## Overview
Analysis of `/workspace/app/app/src/main/java/com/axenvoir/expira/` for a planned major UI redesign + Play Store release.

## File Inventory
- 35 Kotlin source files across `domain`, `data`, `di`, `notification`, `ui`, `CrashReporter`, `MainActivity`, `ExpiraApplication`
- 3 unit test files (`DateUtilsTest`, `FilterTest`, `ModelsTest`)

## Architecture
Single-activity MVVM + Repository + Room + Hilt + WorkManager.
- 9 NavRoutes (Screen.kt:3-18), MainActivity hosts the Compose Scaffold
- ViewModels all `@HiltViewModel` with `StateFlow`+WhileSubscribed
- Clean `domain↔data` separation with mappers in `*RepositoryImpl`

## Key Findings (see message for detail)
- CRITICAL: `NoteEditViewModel.kt:80` references `_userEditedTitle` (declared line 90) → NPE on edit-existing-note
- Localization bug: Turkish seed categories hardcoded in `ProductRepositoryImpl.kt:72-76`
- Release not hardened: minify disabled, lint abortOnError=false
- `CrashDisplayActivity` is dead code; no Crashlytics
- Room DB plaintext; no SQLCipher
- Recompose waste in `ProductListScreen.kt:183`

Full report posted to board.
