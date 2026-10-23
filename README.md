# WidgetDemo

A Mobile Application Development midterm project by a team of three, built with **Java and XML**.

**Topic 02 — Sub-topic 3.1: App Widgets.** The supplied Topic 02 workbook lists App Widgets under section 3, with these five demos under 3.1.

## Widgets

| Widget | Planned feature | Status |
| --- | --- | --- |
| Clock | Display a digital or analog clock | Implemented |
| Weather | Update weather data from an API | Implemented |
| To-Do List | Add and remove tasks | Implemented |
| Music Player | Play and pause music | Implemented |
| Event Calendar | Display upcoming events | Implemented |

The project currently contains a starter activity displaying "Hello World!". No widgets are registered yet. Build and device testing have not been verified during repository setup.

## Getting Started

```bash
git clone https://github.com/xiongfei2011/widget-demo-midterm.git
cd widget-demo-midterm
```

1. Open the project folder in Android Studio and wait for Gradle Sync.
2. Use the SDK and build settings defined in the project files, including `app/build.gradle.kts`.
3. Choose a device that meets the project's `minSdk`:
   - **Phone:** enable USB debugging, connect it, and approve the debugging prompt.
   - **Emulator:** create or start a virtual device in Android Studio's Device Manager.
4. Select the `app` run configuration and the device, then click **Run**.

Weather API setup will be documented when implemented. Do not commit API keys, credentials, `local.properties`, or generated build files.

## Adding a Widget

Once widgets are implemented and the app is installed:

1. Long-press an empty area on the home screen.
2. Select **Widgets**, then find **WidgetDemo**.
3. Drag or add a widget to the home screen and test its features.

The exact steps depend on the launcher. WidgetDemo will not offer widgets until their implementation is added.

## Team Workflow

Create branches in this order: **main → dev → feature branch**.

- `main`: stable project milestones. Commit the initial README here before creating `dev`.
- `dev`: shared development and integration branch.
- `feature/<name>`: one feature or task, created from the latest `dev`.

After `dev` is published, each member can start work from a fresh clone:

```bash
git switch --track origin/dev
git switch -c feature/clock-widget
```

For later tasks, switch to the existing `dev` branch and run `git pull --ff-only origin dev` before creating a feature branch. Save any uncommitted work before switching branches.

Commit only relevant files, push the feature branch, and open a **PR into dev**. Another member reviews it before merging. Merge tested milestones through a **PR from dev into main**. Do not force push shared branches.

**Members and responsibilities:** 

| Thành viên | Họ tên / MSSV / GitHub      | Phân công |
| --- |-----------------------------| --- |
| 1 | Vũ Hùng Phi - 524K0008      | Music Player Widget, To-do List Widget |
| 2 | Trần Hoàng Khải - 523V00 03 | Weather Widget, Event Calender Widget |
| 3 | Đinh Ngọc Hoa - 524K0031    | Clock Widget |

## Submission and Rubric



| Deliverable | Requirements | File or link |
| --- | --- | --- |
| Report | Explain App Widgets and how the demos were implemented | To be added |
| Source code | Original demo implementations the team can explain | This repository; demos pending |
| Demo videos | Clearly show the demos; each video must be at most 2 minutes | To be added |



