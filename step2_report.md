# Step 2 Report

## BeerReal

## Team Members

Hendrik Aarma<br>
Gloria Rannik<br>
Kevin Näälik<br>
Tamor Tomson<br>

## What Data is Stored Locally

- User data: A mock current user is stored in memory for session simulation (MockData.currentUser).
- Beer posts: A static list of beer posts is stored in memory for demo purposes (MockData.mockBeerPosts).
- Comments: Comments on beer posts are also held in memory via mock models.
- No persistent storage: There is no use of Room, SharedPreferences, or local files. Data does not survive app termination or device reboot.
- No actual user-generated content or preferences are saved locally.

## Challenges and Solutions

### Challenge 1: Trying to familiarize with Kotlin

Problem: Kotlin was a new programming language for all of us and we didn't exactly know how everything works

Solution: We took time to understand it with the help of course materials

### Challenge 2: Implementing pull to refresh

Problem: At first we couldn't get pull to refresh to work properly

Solution: Changing androidx version solved the issue

### Challenge 3: Setting up data models so they have just enough data

Problem: The data models got too large, and contained duplicate info throughout eachother

Solution: redid the data models after thinking through the whole functionality instead of adding things 1 by 1 while making them

### Challenge 4: Workflow

Problem: Getting used to using a emulator to test out new builds

Solution: We found out using a real android phone with usb debugging was much faster
