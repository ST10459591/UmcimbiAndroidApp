Kholeka Maphalala
ST10459591
OPSC6312 POE

## Introduction
UMCIMBI is a South African mobile application developed to help people find and book service providers for different events. These include weddings, birthdays, graduations, traditional celebrations, baby showers and corporate events.

The application makes event planning easier by bringing different services together in one place. Instead of searching through different websites or social media platforms, customers can use UMCIMBI to find service providers, view their information, communicate with them and request bookings.

UMCIMBI focuses on the South African events industry and aims to provide a simple and convenient experience for both customers and service providers.


## Purpose of the Application
The main purpose of UMCIMBI is to connect customers who are planning events with businesses that offer event-related services.

Customers can browse different services, search for providers, view business profiles and request bookings. They can also contact providers, send messages and save their favourite businesses.

Service providers can register their businesses, display information about their services and manage booking requests from customers.

The application helps reduce the time and effort needed to organise events.

## Application Design
UMCIMBI has a simple and user-friendly design. The application mainly uses black, white and grey colours to create a clean and modern appearance.

Different background images are used to represent event services and make the application more attractive.

The application includes clearly labelled buttons, organised service categories and a bottom navigation menu.

The main screens include:
- Registration and Login
- Customer and Service Provider Dashboards
- Explore Categories
- Search Services
- Service Provider Profiles
- Booking Requests
- My Bookings
- Messages
- Favourites
- Profile and Settings

The design allows customers and service providers to access the features they need without unnecessary complications.

## Main Application Features

## User Registration and Login

UMCIMBI allows customers and service providers to create accounts and log in using their email addresses and passwords.

Firebase Authentication is used to manage user accounts and login details.

After logging in, users can access the features available to their account type.

## Event Categories and Provider Search

Customers can browse event services such as catering, venues, photography, décor, entertainment and transport.

They can also search for providers using a service category, business name or location.

The application displays matching service providers using information stored in Firebase Firestore.

## Service Provider Profiles

Customers can open a provider's profile to view important business information.

This includes the business name, service category, description, location, pricing, availability, contact details and business photographs.

Customers can also contact the provider, send a message, request a booking or add the business to their favourites.

## Business Image Uploads Using Cloudinary

UMCIMBI uses Cloudinary to store and manage images uploaded by service providers.

Service providers can upload photographs of their work to help customers understand the services they offer. These images are stored online using Cloudinary and displayed on the provider's business profile.

This allows customers to view examples of previous work before contacting a provider or requesting a booking.

Cloudinary also helps the application manage business photographs without storing the actual image files directly in Firebase Firestore.

## Booking System

UMCIMBI allows customers to submit booking requests to service providers.

Customers enter details such as the event type, date, location, number of guests and budget.

After submitting a request, customers can view its status under My Bookings.

Service providers can view incoming requests and choose to accept or reject them. The customer's booking status is then updated.

## Messaging

The application includes a messaging feature that allows customers and service providers to communicate.

Customers can ask questions about services, pricing and availability before making a booking.

Conversations can also be accessed through the Messages section.

## Favourites

Customers can save service providers to their favourites.

This allows them to find businesses they are interested in without searching for them again.

## Offline Functionality

UMCIMBI includes local data storage to support access to certain previously saved information.

Room Database is used for local provider data, while Firebase supports retrieving and updating online information.

This helps improve the user experience when the internet connection is limited.

## Multilingual Support

UMCIMBI supports three languages:
- English
- isiZulu
- Sesotho

Users can change their preferred language, and supported parts of the application will display translated text.

This makes the application more accessible to users who prefer different South African languages.

## Profile and Settings

Users can access their profile and settings to manage their account information and preferences.

The settings include personal information, language selection, notification preferences, dark mode and logout.

### Technical Design

UMCIMBI was developed using Kotlin in Android Studio.

XML was used to create the application's screens, layouts, buttons and other interface elements.

Firebase Authentication manages user registration and login, while Firebase Firestore stores information such as provider details, booking requests and messages.

Cloudinary is used to upload, store and manage business photographs. When a service provider uploads an image, Cloudinary stores the image online so it can be displayed on their business profile.

Room Database is used for local storage of selected information.

The application uses Firebase to retrieve service provider information and update booking records.

The main technologies used include:

- Kotlin – application development
- XML – user interface design
- Android Studio – development environment
- Firebase Authentication – user accounts
- Firebase Firestore – online database
- Cloudinary: Used to upload and store business photographs.
- Room Database – local data storage
- GitHub – version control

## GitHub and Version Control

GitHub is used to manage the UMCIMBI project files and track changes made during development.

Version control helps keep the project organised and makes it easier to identify changes made to the code.

The project contains Kotlin files, XML layouts, drawable resources and other files needed to run the application.


This screenshot shows the UMCIMBI project repository and the files used during development.

## GitHub Actions and Automated Testing

GitHub Actions can be used to automate the building and testing of the Android application.

Automated workflows help developers check whether the project builds correctly after changes are pushed to the repository.

During development, UMCIMBI was also checked through manual testing of its main features.

The main functions checked included registration, login, provider searching, booking requests, messaging, favourites and language selection.

This screenshot should show the result of an automated build or test workflow.

## User Interface and User Experience

UMCIMBI was designed to be simple and easy to navigate.

The application uses a consistent layout, readable text, clear buttons and event-related background images.

The bottom navigation menu allows users to move between Home, Explore, Bookings, Messages and Profile.

The application also includes input fields, error messages and booking statuses to help users understand what is happening.

The application was tested using an Android emulator and Android application to check its appearance, navigation and main functionality.

## Application Screenshots and Functional Testing

## 9.1 Explore Categories
<img width="1080" height="2400" alt="Screenshot_20261106_134738" src="https://github.com/user-attachments/assets/be36b416-8ea1-4fb5-8524-1e9d2493045e" />

This screen displays different event service categories. Customers can choose the type of service they need for their event.

**9.2 Service Providers**

<img width="1080" height="2400" alt="Screenshot_20261106_134941" src="https://github.com/user-attachments/assets/585cb277-d297-4b83-b83d-9fe0fdaf9334" />

This screen displays service providers under the selected category. Customers can view business names, locations, prices and availability.

**9.3 Search Services**

<img width="1080" height="2400" alt="Screenshot_20261106_135016" src="https://github.com/user-attachments/assets/bb2bf16d-a6d9-4540-8ae6-e266389ac168" />

This screen allows customers to search for service providers. The application displays matching businesses using information retrieved from Firebase Firestore.

**9.4 Provider Profile**

<img width="1080" height="2400" alt="Screenshot_20261106_135236" src="https://github.com/user-attachments/assets/8be08b12-511e-47ec-9863-89a4dd119d90" />

This screen displays information about a selected service provider, including business details, photographs, pricing, availability and contact information.

Customers can also send messages, request bookings and save providers to their favourites.

**9.5 Contact Provider**

<img width="1080" height="2400" alt="Screenshot_20261106_135320" src="https://github.com/user-attachments/assets/558bc10b-2f4e-4562-bcd4-d4b8af6442f1" />

This screenshot shows that customers can access the phone's dialler using the contact information provided by the business.

**9.6 Messaging**

<img width="1080" height="2400" alt="Screenshot_20261106_135419" src="https://github.com/user-attachments/assets/3edc523f-2584-44c6-8eac-9b8e11053754" />
<img width="1080" height="2400" alt="Screenshot_20261106_135817" src="https://github.com/user-attachments/assets/4deeecc5-8674-452f-9dd6-89887eafdc0f" />

These screens show how customers can communicate with service providers and view their conversations.

**9.7 Booking Request**

<img width="1080" height="2400" alt="Screenshot_20261106_135541" src="https://github.com/user-attachments/assets/bac92ba2-21d5-4eb5-9499-680d9add5fe5" />

This screen allows customers to enter their event information and submit a booking request to a service provider.

**9.8 Booking Status**

<img width="1080" height="2400" alt="Screenshot_20261106_135704" src="https://github.com/user-attachments/assets/8125d8f7-fa5c-456a-b63a-f3631bc7538b" />

This screenshot shows the customer's booking request with a pending status after submission.

**9.9 Provider Booking Management**

<img width="1080" height="2400" alt="Screenshot_20261106_163538" src="https://github.com/user-attachments/assets/9b812d61-c8b9-49fc-825c-d6ea778b604a" />

This screen shows how a service provider can view a customer's booking request and choose to accept or reject it.

**9.10 Booking Confirmation**

<img width="1080" height="2400" alt="Screenshot_20261106_163755" src="https://github.com/user-attachments/assets/6cbdec30-c826-4e78-a890-db48ed10060b" />

This screenshot shows that the customer's booking status changes to accepted after the service provider approves the request.

**9.11 Favourites**

<img width="1080" height="2400" alt="Screenshot_20261106_135835" src="https://github.com/user-attachments/assets/f5fb1d02-256d-4fa1-a20e-87d59cfe18e8" />

This screen displays service providers saved by the customer for future reference.

**9.12 Profile and Settings**

<img width="1080" height="2400" alt="Screenshot_20261106_135911" src="https://github.com/user-attachments/assets/56c62c7f-71ab-46ef-94f4-f20ed3981e23" />
<img width="1080" height="2400" alt="Screenshot_20261106_140000" src="https://github.com/user-attachments/assets/545f1507-ee09-4f3f-9d85-2bc6776f015e" />

These screens show the user's profile information, language selection, notification preferences and appearance settings.

**9.13 Dark Mode**

<img width="1080" height="2400" alt="Screenshot_20261106_140038" src="https://github.com/user-attachments/assets/28d1295d-60b9-4436-865f-e3f79eb94f96" />

This screenshot demonstrates the appearance of the application when dark mode is enabled.

**9.14 Multilingual Support**

<img width="1080" height="2400" alt="Screenshot_20261106_140111" src="https://github.com/user-attachments/assets/f10078da-5fb0-4815-a7d6-03ecfa531dcb" />
<img width="1080" height="2400" alt="Screenshot_20261106_140123" src="https://github.com/user-attachments/assets/9e843f12-4982-4100-8964-4d5015f2ab59" />

These screenshots demonstrate how the application displays translated text when Sesotho is selected.

### 10. Testing and Results

Manual testing was carried out to check whether the application's main features worked as expected.

The following features were checked:

| Feature Tested | Expected Result | Observed Result |
|---|---|---|
| Registration and Login | Users can access their accounts | Worked during testing |
| Explore Categories | Categories display available providers | Providers were displayed |
| Search Services | Matching providers appear | Matching businesses appeared |
| Provider Profiles | Business information is displayed | Provider details were shown |
| Booking Requests | Customers can submit requests | Requests were submitted |
| Booking Management | Providers can accept or reject requests | Booking status was updated |
| Messaging | Users can communicate with providers | Messages were displayed |
| Favourites | Customers can save providers | Saved provider appeared |
| Language Selection | Supported text changes language | Sesotho and IsiZulu translations appeared |
| Settings | Users can manage account preferences | Settings screens were displayed |

The screenshots provide evidence of the main features that were tested.

### Challenges and Improvements

During development, some problems were experienced with displaying service provider information and updating parts of the user interface.

One issue involved the search feature displaying information that was not stored in the application's Firebase database. This was corrected by retrieving provider information directly from Firebase Firestore.

Another challenge involved translating some parts of the application into different languages. The text resources were updated to improve multilingual support.

Testing helped identify these issues and improve the application before submission.

Further improvements could include more advanced search filters, additional payment options, improved notifications and a more complete quotation comparison system.

### Conclusion

UMCIMBI is a mobile application developed to make event planning easier for customers and service providers in South Africa.

The application allows customers to browse services, search for providers, view business profiles, communicate with businesses and submit booking requests.

## References


Service providers can advertise their services and manage customer booking requests.

The application also includes favourites, account settings, local data storage and multilingual support.

Overall, UMCIMBI demonstrates how a mobile application can bring different event services together on one platform and make the event planning process more convenient.
