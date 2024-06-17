#Prayer Time Remainder for Muslim Relegion

Ahmet Mahir Demirelli - 

SDK Levels:

    Min SDK: 26

    Compile SDK: 34

    Target SDK: 34

How it Works:

        First time use:
            * When program starts it asks for notification permission(for API Level >=33), ideally user must give permission because notification is used for giving chance to user for stopping the alarm.
            * Then user tops on the field that says "No Saved Location" then chooses "Add New Location" .
            * Then AddNewLocation page opens and it connects to "https://namazvakitleri.diyanet.gov.tr/tr-TR" by WebView, user will chose the country, city field and state if it's asked. Then user will press the "Add Location" button.
            * Then a dialog will open to ask for location name, user can use any name but it should not contain ('\', '/', ':', '*', '?', '"', '<', '>', '|', '.') these characters.
            * Then user will press on the phones back button and returns the Main Page.
            * Now user can see all the five prayer times for the day ( for the next day if it's after last prayer time).
        Normal time use:
            * User can change language with the switch between Turkish and English
            * User can press the location field then add new location or can choose location from already added locations(user also can delete location or change the name of the location).
            * To set the alarms, user will open the switchs by according to preferences then push the "Set Alarm" button, alarms will go off even the app is closed.
            * User has to restart the phone to disable all the alarms that are set.
Nasıl Çalışır:

        İlk kez kullanım:
            * Program başladığında bildirim izni ister (API Seviyesi >=33 için), ideal olarak kullanıcı izin vermelidir çünkü bildirim kullanıcıya alarmı durdurma şansı vermek için kullanılır.
            * Daha sonra kullanıcı "Kayıtlı Konum Yok" yazan alanın üzerine gelir ve ardından "Yeni Konum Ekle"yi seçer.
            * Daha sonra AddNewLocation sayfası açılacak ve WebView ile "https://namazvakitleri.diyanet.gov.tr/tr-TR" adresine bağlanacak, kkullanıcı ülke, şehir seçer ve gerekliyse ilçeyi de seçer. Daha sonra kullanıcı "Konum Ekle" butonuna basar.
            * Daha sonra konum adını sormak için bir iletişim kutusu açılacaktır, kullanıcı herhangi bir adı kullanabilir ancak bu ad ('\', '/', ':', '*', '?', '"', '<' , '>', '|', '.') karakterlerini içermemelidir.
            * Daha sonra kullanıcı telefonun geri tuşuna basarak ve Main Page'e dönecektir.
            * Artık kullanıcı o günkü beş namaz vaktinin tamamını görebilir (eğer son namaz vaktinden sonra ise ertesi gün için namaz vakitlerini görür).
        Normal zamanlı kullanım:
            * Kullanıcı Türkçe ve İngilizce arasında geçiş yaparak dili değiştirebilir
            * Kullanıcı konum alanına basabilir, ardından yeni konum ekleyebilir veya önceden eklenmiş konumlardan konum seçebilir (kullanıcı ayrıca konumu silebilir veya konumun adını değiştirebilir).
            * Alarmları ayarlamak için kullanıcı tercihlerine göre switch'leri açar ve ardından "Alarm Ayarla" düğmesine basar, uygulama kapatılsa bile alarmlar çalacaktır.
            * Ayarlanan tüm alarmları devre dışı bırakmak için kullanıcının telefonu yeniden başlatması gerekir.
