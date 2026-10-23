# WidgetDemo

A Mobile Application Development midterm project by a team of three, built with **Java and XML**.

**Topic 02 — Sub-topic 3.1: App Widgets.** The supplied Topic 02 workbook lists App Widgets under section 3, with these five demos under 3.1.

## Widgets

| Widget | Features | Status |
| --- | --- | --- |
| Clock | Digital time and date, time-zone selection, day/night themes and solar arc | Implemented |
| Weather | Current weather from OpenWeather, city selection and manual refresh | Implemented |
| To-do List | Add tasks, toggle completion and delete selected tasks | Implemented |
| Music Player | Play/pause, previous/next, track information and cover art | Implemented |
| Event Calendar | Monthly calendar, date-based event viewing, addition and deletion | Implemented |

All five widgets are registered in a single application. MainActivity
displays the application name, while the widgets provide their interfaces
and controls on the home screen.

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

The Weather Widget retrieves current conditions from OpenWeather and
requires an internet connection and a valid API key. Select a city when
adding the widget, or tap the city name to change it.

## Adding a Widget

After installing the app:

1. Long-press an empty area on the home screen.
2. Select **Widgets**, then find **WidgetDemo**.
3. Drag or add a widget to the home screen and test its features.

The exact steps depend on the launcher. WidgetDemo will not offer widgets until their implementation is added.

## Known Limitations
- **Clock:** Sunrise and sunset are approximate calculations using fixed
  coordinates. Theme and solar arc updates depend on system scheduling.
  Narrow layouts may cause the AM/PM indicator to wrap.
- **Weather:** Previous weather results are not retained when a request
  fails. Offline caching and forecasts are not supported. The configured
  update interval does not guarantee updates every ten minutes, and
  asynchronous request lifecycle handling needs improvement.
- **To-do List:** The shared list supports up to ten tasks. Title editing,
  filtering and deletion undo are not available. All widget instances
  share both tasks and deletion selection.
- **Music Player:** Playback uses a fixed local playlist. The current track
  and playback position are not restored after a service restart.
  Progress display and interactive seeking are not supported.
- **Event Calendar:** The fixed five-row grid omits dates in months requiring
  six rows. Each day cell displays at most two event titles. Time-input
  validation and day-boundary handling need improvement. Reminders and
  device-calendar synchronization are not supported.

Compatibility across devices and launchers, lifecycle behavior and
consistency between multiple widget instances have not been
systematically verified.

## Team Workflow

Create branches in this order: **main → dev → feature branch**.

- `main`: stable project milestones, integrated through pull requests from `dev`.
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

| Member | Name / Student ID      | Responsibilities |
| --- |-----------------------------| --- |
| 1 | Vũ Hùng Phi - 524K0008      | Music Player Widget, To-do List Widget |
| 2 | Trần Hoàng Khải - 523V0003 | Weather Widget, Event Calender Widget |
| 3 | Đinh Ngọc Hoa - 524K0031    | Clock Widget |

## Submission and Rubric



| Deliverable | Requirements | File or link |
| --- | --- | --- |
| Report | Explain App Widgets and how the demos were implemented | To be added |
| Source code | Original demo implementations the team can explain | [Link for Source Code](https://github.com/xiongfei2011/widget-demo-midterm) |
| Demo videos | Clearly show the demos; each video must be at most 2 minutes | [Link for Demo](https://drive.google.com/drive/folders/1k2P7rqR7p-Il7xk1BJV4kdpk-zrfb-nI) |



