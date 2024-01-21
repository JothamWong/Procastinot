# ScrLk - Curb Your Doomscrolling

## Inspiration
The "doomscrolling" epidemic has become a significant issue among teenagers today, leading to increased stress and a decline in personal, intellectual, and social development. 

Modern solutions like app time limit controls are often ineffective, because we press 'continue anyway' when the time limits up. 

Our aim was to develop a more robust solution to this problem based on cutting edge science:

<img width="740" alt="amazing" src="https://github.com/JothamWong/Procastinot/assets/70333123/8a651d21-7f2d-4bf3-a612-7b0854a6fc2c">

## What it does
ScrLk is an app designed to combat excessive social media usage. Users can blacklist apps by setting a daily usage time limit. Once this limit is reached, the app disrupts the scrolling functionality.

## Demo
You give this app all the permissions of ~~malware~~ a good faith accessibility app.

https://github.com/JothamWong/Procastinot/assets/70333123/0bd1ec94-ff24-4683-9404-77bad4771a4e

Now a time limit will be **enforced**.

https://github.com/JothamWong/Procastinot/assets/70333123/73ce3da2-7574-4656-8486-55426fc56adf

For example, if a user tries to scroll through Reddit after hitting their limit, the app will scroll back to the top, preventing further content consumption.

https://github.com/JothamWong/Procastinot/assets/70333123/c84e45fb-3491-48c7-b8d5-a68b772e3617

## How We Built It
We leveraged Jetpack Compose to register ScrLk as an accessibility application, granting it permission to handle scroll events and display overlays on other applications. The app runs two main services:

1. **Usage Tracking Service:** Tracks the time spent in each app by updating during scroll events or when the app is exited.
2. **Overlay Service:** Displays a countdown timer for the daily limit at the bottom of blacklisted applications.

## What We Learned
This project was a fast and dirty dive into Android development, who knew android's documentation was such a pita

## Built With
- Android
- Jetpack Compose
- A mix of determination, pain, and suffering

## Installation
Would not reccomend in good faith. This requests for way too many permissions that anyone who values their cyber security would avoid. If you really want to we'd reccomend an emulator to try it out.. glhf

---
For more information and updates (probably not), stay tuned to our [DevPost project page!](https://devpost.com/software/scrlk)

