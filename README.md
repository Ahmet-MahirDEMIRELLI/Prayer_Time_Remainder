#Prayer Time Remainder for Muslim Relegion

Ahmet Mahir Demirelli - 

SDK Levels:

    Min SDK: 26

    Compile SDK: 34

    Target SDK: 34

How it Works (En):

        First time use:
                * When program starts it asks for notification permission(for API Level >=33), ideally user must give permission because notification is used for giving chance to user for stopping the alarm.
                * Then user tops on the field that says "No Saved Location" then chooses "Add New Location" .
                * Then AddNewLocation page opens ant it connects to "https://namazvakitleri.diyanet.gov.tr/tr-TR", user will chose the country, city field and state if it's prometed. Then user will press the "Add Location" button.
                * Then a dialog will open to ask for location name, user can use any name but it should not contaion ('\', '/', ':', '*', '?', '"', '<', '>', '|', '.') these characters.
                * Then user will press on the phones back button and returns the Main Page.
                * Now user can see all the five prayer times for the day ( for the next day if it's after last prayer time).
        Normal time use:
                * User can press the location field then add new location or can choose location from already added locations(user also can delete location or change the name of the location).
                * To set the alarms, user will open the switchs by according to preferences then push the "Set Alarm" button, alarms will go off even the app is closed.
                * User has to restart the phone to disable all the alarms that are set.
