# FaceFit - Your Virtual Eyewear Try-On Experience

<table>
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/7a99d3b0-b79d-4a45-8681-a96d97bd2992" width="250" height="500"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/4b761047-c500-4696-aaa7-2b1f676bffaf" width="250" height="500"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/af40fa3a-58e6-485f-9a82-58a5054d21ae" width="250" height="500"/>
    </td>
  </tr>
   <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/25a79e89-e20b-41fb-b62b-1b0ca7cf0518" width="250" height="500"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/ab05581b-3da1-4ecf-9fa9-f4282778d6e0" width="250" height="500"/>
    </td>
<!--       <td>
      <img src="https://github.com/user-attachments/assets/a06e5472-c786-4506-b72f-dc1826660b71" width="230" height="500"/>
    </td> -->
  </tr>
</table>

## About the Project

FaceFit is an innovative Android application that leverages Augmented Reality (AR) to transform the online eyewear shopping experience. It addresses the common challenge of customers being unable to physically try on glasses before purchase, allowing users to visualize how different styles look on their face in real-time. FaceFit aims to enhance customer confidence and reduce return rates by offering a realistic virtual try-on solution.

## FaceFit (End-User) App

The FaceFit end-user application is a mobile app for Android that allows users to virtually try on glasses using augmented reality.

### Features

* **Seamless AR Try-On:** Offers a smooth and intuitive augmented reality try-on experience for eyewear.
* **Realistic Experience:** Provides realistic virtual try-ons of glasses, significantly enhancing the online shopping experience by showing how frames look on the user's face.
* **Enhanced Online Shopping:** Aims to boost customer confidence and satisfaction by allowing pre-purchase visualization of products.

## Technologies

* **Compose:** A modern toolkit for building native Android UI.
* **Kotlin:** The primary programming language for Android development.
* **ARCore:** Google's platform for building augmented reality experiences, used for real-time face tracking and 3D mesh generation.
* **OpenGL:** A powerful cross-platform graphics API utilized for high-quality rendering of 3D glasses.
* **Blender:** A 3D creation suite used for modeling the 3D glasses assets.

## Technical Details (Android App)

### Android Architecture

FaceFit adheres to a robust and scalable architectural design to ensure maintainability, testability, and performance.

#### Clean Architecture

The application is structured using Clean Architecture principles, promoting a clear separation of concerns:

* **Data Layer:** Responsible for handling all data sources, including fetching data from remote APIs and managing local storage.
* **Domain Layer:** Contains the core business logic of the application, remaining independent of any specific framework or user interface details.
* **App Layer (Presentation):** Deals with the user interface elements and ViewModels, interacting with the Domain layer to manage and display UI state.

#### MVVM Pattern

The Model-View-ViewModel (MVVM) structural pattern is implemented for the UI, ensuring effective decoupling and reactivity:

* **Separation:** It distinctly separates the UI logic (ViewModel) from the UI layout (View).
* **ViewModel:** Exposes data to the View, typically through observable data holders such as `LiveData` or `StateFlow`.
* **View:** Observes the ViewModel for any changes in data and updates the user interface accordingly.

The MVVM pattern makes the UI reactive and significantly decouples it from the underlying business logic, which simplifies lifecycle management and improves the testability of UI-related logic.

## Augmented Reality (AR) Implementation and AI Model

FaceFit's virtual try-on experience is built upon a foundation of specific technologies that enable seamless and realistic interactions.


### Core Technologies for AR and Face Tracking

<table>
  <tr>
    <td>
      <img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/b080dbf6-2bca-4882-b9a6-e2e78bc48838" />
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/d16abf5f-7f21-44e9-849c-c0d40d7d40d0" width="250" height="500"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/bb54e9ca-fe59-4aaf-b3b1-157b53eff9a1" width="250" height="500"/>
    </td>
  </tr>


* **ARCore: Real-time Face Tracking and 3D Mesh Generation**
    ARCore plays a pivotal role. Its primary function is to perform **real-time face tracking**, continuously identifying and monitoring the user's face within the camera feed. Crucially, ARCore generates a **3D mesh of the user's face**. This dynamic digital representation captures the contours and structure of the face. This "FaceMesh" provides the essential spatial data required to accurately position and orient the virtual glasses, ensuring they appear naturally worn and adapt to facial movements and expressions.

* **OpenGL: High-Quality Rendering of 3D Glasses**
    Once ARCore provides the precise facial position and orientation via the 3D mesh, **OpenGL** takes over. OpenGL is a powerful cross-platform graphics API used for rendering 2D and 3D vector graphics. In FaceFit, OpenGL is responsible for taking the pre-modeled 3D glasses and rendering them realistically onto the live camera stream. It ensures perfect alignment with the tracked face mesh, handling critical aspects like lighting, shading, and perspective to create a highly convincing virtual try-on.

* **Blender: Tool for Modeling the 3D Glasses**
    The digital assets themselves, the 3D models of the glasses, are created using **Blender**. Blender is a free and open-source 3D creation suite used by designers or 3D artists to meticulously model various eyewear styles, which are then integrated into the FaceFit application for the AR rendering process.

### Implied AI Model Used

The provided information does not explicitly name a specific "AI model" for face tracking or other functionalities. However, the core capabilities of ARCore, particularly its "real-time face tracking and 3D mesh generation," are fundamentally powered by advanced **computer vision and machine learning (AI) algorithms**.

These sophisticated AI/ML models, embedded within ARCore, analyze the live camera input to:
* Identify and detect human faces.
* Recognize key facial landmarks (e.g., eyes, nose, mouth).
* Estimate head pose and orientation in 3D space.
* Construct the dynamic 3D mesh of the face, which is crucial for the accurate placement of virtual objects like glasses.
