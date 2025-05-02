[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/K7Dk3ZKR)

🔍 Overview
BudgetTrackerApp is an Android application developed to help users manage and track their personal expenses efficiently. It allows users to log expenses, set monthly budget goals, and review summaries with category-wise insights and attached photos.

✅ Core Features
1. Login Screen
Users log in using a username and password.

Simple local authentication; no external server is used.

2. Home Screen
Main navigation hub with buttons to:

Add a new expense

View a summary of all expenses

Set and save monthly budget goals (min and max)

3. Add Expense
Users can enter:

Expense amount

Description

Date and time (via pickers)

Category (from a spinner/dropdown)

Optional photo from local storage

Expense details are saved locally using Room Database.

4. Summary Screen
Displays all saved expenses in a scrollable list.

Shows:

Amount

Description

Category

Date and time

Attached photo (if any)

Expenses are grouped and can be filtered by category or date range (if implemented).

5. Budget Goals
Users can enter:

Minimum monthly budget

Maximum monthly budget

Saved goals are stored using RoomDB and used for tracking against actual spending.

Useful for monitoring if user is staying within their budget.


🛠 Technologies Used
Kotlin for logic and UI interactions

AndroidX Navigation for screen navigation

RoomDB for local data persistence

CardView and RecyclerView for clean UI presentation

Image picker for optional photo attachment


🗃 Project Structure
LoginFragment.kt – Login logic

HomeFragment.kt – Main navigation hub

AddExpenseFragment.kt – Expense entry form

SummaryFragment.kt – Shows all expenses

GoalEntity, GoalDao, AppDatabase – RoomDB setup for goals and expenses

activity_main.xml, nav_graph.xml, and fragment layouts – UI


💾 Data Storage
Uses Room Database to store:

Expenses

Categories

Budget goals

Data persists locally across app restarts.


📷 Photo Attachment
Users can attach a photo from local device storage.

Photos are displayed with expenses in the summary screen.

Supports common image types like JPG, PNG.



💡 Usage Example
User logs in.

On the Home screen, sets a monthly budget goal.

Adds an expense with category, photo, and details.

Views a visual summary of expenses.

Compares spending against budget goals.
