/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uts.wsd.soap.client;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import static java.lang.System.exit;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import uts.wsd.*;

/**
 *
 * @author sawicky
 */
public class BookingClient {
 
     public static void main(String[] args) throws Exception_Exception, IOException_Exception, JAXBException, JAXBException_Exception{
        //Init the collections we will be using
        Student student = null;
        Tutor tutor = null;
        Students students = null;
        Tutors tutors = null;
        BookingApp locator = new BookingApp();
        BookingsSoap bapp = locator.getBookingsSoapPort();
        StudentApplication sapp = bapp.getStudentApp();
        TutorApplication tapp = bapp.getTutorApp();
        Scanner in = new Scanner (System.in);
        System.out.println("Please login");
        while (tutor==null && student==null) 
        {
            System.out.print("Email : ");
            String email = in.nextLine();
            System.out.print("Password :");
            String pass = in.nextLine();
            student = bapp.sLogin(email, pass);
            tutor = bapp.tLogin(email, pass);
        }
        //We can check whether our user is a tutor or student by checking the object, we ran both logins with the same user,pass but only one of them returned not null
        if (tutor!=null || student!=null) {
            if (student!=null) {
                System.out.println("Welcome Student "+student.getName());
            }
            else if (tutor!=null) {
                System.out.println("Welcome Tutor "+tutor.getName());
                System.out.println("Your availability: "+tutor.getAvailability());
            }
            System.out.println("Please select an option");
            System.out.println("1. View bookings");
            System.out.println("2. Create a booking");
            System.out.println("3. Cancel a booking");
            System.out.println("4. Cancel account");
            System.out.println("5. Edit account");
            //Additional option for a tutor
            if (tutor!=null) {
                System.out.println("6. Complete a booking");
            }
            System.out.println("0. Exit application");
            //Init our bookings
            Bookings bookings = bapp.displayBookings();
            int input = -1;
            //0 will be our sentinel to end program.
            while (input!=0) {
                input = in.nextInt();
                switch(input) {
                    case 1:
                        //For each booking, list it's details.
                        for (Booking b : bookings.getBooking()) {
                            System.out.println("################## Booking ID: "+b.getId()+ " ##################");
                            System.out.println("Student Email: "+b.getStudentEmail()+"\nStudent Name: "+b.getStudentName()+"\nTutor Email: "+b.getTutorEmail()+"\nTutor Name: "+b.getTutorName()+"\nStatus: "+b.getStatus());
                            System.out.println();
                        }
                        menu(tutor);
                        break;
                    case 2:
                        //Different methods for tutor, student
                        if (tutor!=null) {
                            System.out.println("Enter a student name to book");
                            String name = in.next();
                            //Check your input string for a match against all students
                            for (Student s : bapp.getStudents().getStudent()) {
                                //If there is a match, and you(tutor) are available, create the booking.
                                if (name.equals(s.getName())) {
                                    System.out.println("Found student with the name: "+name);
                                    System.out.println("Students email: "+s.getEmail());
                                    if (tutor.getAvailability().equals("Available")) {
                                        //Get a unique ID of the booking by interating through all bookings again and incrementing the ID.
                                        int id = 0;
                                        for (Booking bs : bookings.getBooking()) {
                                            if (id == bs.getId()) {
                                                id++;
                                            }
                                        }
                                        //Use our createbooking method from our Booking Soap service, then add it to the list of bookings that we also take from the soap service.
                                        Booking newBooking = bapp.createBooking(id, s.getEmail(), s.getName(), tutor.getEmail(), tutor.getName(), tutor.getSubject(), "Active");
                                        Bookings b = bapp.displayBookings();
                                        b.getBooking().add(newBooking);
                                        //Hack method: Could not retrieve a specific tutor from the list of tutors via Soap service, so instead, iterated through all tutors until a match was 
                                        //found with your current details, then set the availability.
                                        for (Tutor t : tapp.getTutors().getTutor()) {
                                            if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                t.setAvailability("Unavailable");  
                                            }
                                        }
                                        //Use the update methods from Soap that marshal objects back to xml.
                                        bapp.updateTutors(tapp.getTutors(), tapp.getFilePath());
                                        bapp.updateBookings(b, bapp.getFilePath());
                                        System.out.println("Booking created! Details: ");
                                        System.out.println("################## Booking ID: "+newBooking.getId()+ " ##################");
                                        System.out.println("Student Email: "+newBooking.getStudentEmail()+"\nStudent Name: "+newBooking.getStudentName()+"\nTutor Email: "+newBooking.getTutorEmail()+"\nTutor Name: "+newBooking.getTutorName()+"\nStatus: "+newBooking.getStatus());
                                        System.out.println();
                                        menu(tutor);
                                        
                                    }  else {
                                        System.out.println("You are unavailable!");
                                        System.out.println();
                                        menu(tutor);
                                    }
                                }
                            }
                        } else if (student!=null) {
                            System.out.println("Enter a tutor name to book");
                            String name = in.next();
                            for (Tutor t : tapp.getTutors().getTutor()) {
                                if (name.equals(t.getName())) {
                                    System.out.println("Found tutor with the name: "+name);
                                    System.out.println("Tutors email: "+t.getEmail());
                                    //Same method as the tutor, but instead checks your target tutors availability and fails if unavailable.
                                    if (tutor.getAvailability().equals("Available")) {
                                        int id = 0;
                                        for (Booking bs : bookings.getBooking()) {
                                            if (id == bs.getId()) {
                                                id++;
                                            }
                                        }
                                        Tutors ts = tapp.getTutors();
                                        Bookings b = bapp.displayBookings();
                                        tutor.setAvailability("Unavailable");
                                        bapp.updateTutors(ts, tapp.getFilePath());
                                        bapp.updateBookings(b, bapp.getFilePath());
                                    }  else {
                                        System.out.println("Tutor is unavailable!");
                                        System.out.println();
                                        menu(tutor);
                                    }  
                                }
                            }
                        }
                        break;
                    case 3:
                        System.out.println("Enter ID of booking to cancel");
                        
                        int cancel = in.nextInt();
                        Bookings sb = bapp.displayBookings();
                        //For each booking, if there is an ID that matches your input ID, and you own it (via crosschecking your name, email) then set the attached tutors avail to Available and set the booking to Cancelled
                        for (Booking b : sb.getBooking()) {
                            if (cancel==b.getId()) {
                                if (student!=null) {
                                    if (student.getName().equals(b.getStudentName()) && student.getEmail().equals(b.getStudentEmail())) {
                                        System.out.println("Booking cancelled!");
                                        System.out.println();
                                        for (Tutor t : tapp.getTutors().getTutor()) {
                                            if (b.getTutorEmail().equals(t.getEmail()) && b.getTutorName().equals(t.getName())) {
                                                t.setAvailability("Available");  
                                            }
                                        b.setStatus("Cancelled");
                                        }
                                    bapp.updateTutors(tapp.getTutors(), tapp.getFilePath());
                                    bapp.updateBookings((sb), bapp.getFilePath());    
                                    menu(tutor);
                                    } else {
                                        System.out.println("You do not own this booking!");
                                        System.out.println();
                                        menu(tutor);
                                    }

                                }
                                if (tutor!=null) {
                                    if (tutor.getName().equals(b.getTutorName()) && tutor.getEmail().equals(b.getTutorEmail())) {
                                        System.out.println("Booking cancelled!");
                                        System.out.println();
                                        for (Tutor t : tapp.getTutors().getTutor()) {
                                            if (b.getTutorEmail().equals(t.getEmail()) && b.getTutorName().equals(t.getName())) {
                                                t.setAvailability("Available");  
                                            }
                                        b.setStatus("Cancelled");
                                        }
                                    bapp.updateTutors(tapp.getTutors(), tapp.getFilePath());
                                    bapp.updateBookings((sb), bapp.getFilePath());    
                                    menu(tutor);
                                    } else {
                                        System.out.println("You do not own this booking!");
                                        System.out.println();
                                        menu(tutor);
                                    }

                                }
                            } else {
                                System.out.println("Booking ID not found!");
                                System.out.println();
                                menu(tutor);
                            }
                        }
                        break;
                    case 4:
                        Students bs = bapp.getStudents();
                        Tutors bt = bapp.getTutors();
                        Bookings bookingSearch = bapp.displayBookings();
                        //First, iterate through all bookings that are assigned to you and set them to cancelled.
                        if (student!=null) {
                            for (Booking b : bookingSearch.getBooking()) {
                                if (student.getName().equals(b.getStudentName()) && student.getEmail().equals(b.getStudentEmail())) {
                                    b.setStatus("Cancelled");
                                }
                            }
                            //Next, iterate through the list of objects, find yours then remove it from the array we took from our Booking Soap service.
                             for (Student s : bs.getStudent()) {
                                    if (student.getEmail().equals(s.getEmail()) && student.getPassword().equals(s.getPassword())) {
                                        bs.getStudent().remove(s); 
                                        break;
                                    }
                                    
                                }
                             //Marsha the bookings and objects xml.
                            bapp.updateStudents((bs), sapp.getFilePath());
                            bapp.updateBookings((bookingSearch), bapp.getFilePath()); 
                        }
                        else if (tutor!=null) {
                            for (Booking b : bookingSearch.getBooking()) {
                                if (tutor.getName().equals(b.getTutorName()) && tutor.getEmail().equals(b.getTutorEmail())) {
                                    b.setStatus("Cancelled");
                                }
                            }
                             for (Tutor s : bt.getTutor()) {
                                    if (tutor.getEmail().equals(s.getEmail()) && tutor.getPassword().equals(s.getPassword())) {
                                        bt.getTutor().remove(s);
                                        break;
                                    }
                                }
                            bapp.updateTutors((bt), tapp.getFilePath());
                            bapp.updateBookings((bookingSearch), bapp.getFilePath()); 
                        }
                        System.out.println("Account Cancelled!");
                        System.out.println();
                        System.exit(0);
                        break;
                    case 5:
                        //Extra option 5 for tutors.
                        Tutors editTutors = bapp.getTutors();
                        Students editStudents = bapp.getStudents();
                        System.out.println("What do you want to edit?");
                        System.out.println("1. Name");
                        System.out.println("2. Email");
                        System.out.println("3. Password");
                        System.out.println("4. Date of Birth");
                        if (tutor!=null) {
                            System.out.println("5. Specialty Subject");              
                        }
                        System.out.println("0. Back to main menu");
                        int choice = in.nextInt();
                        Scanner inEdit = new Scanner (System.in);
                        switch (choice) { 
                            case 1:
                                //Simple method. Iterate through list of objects until there is match with your current login, then call the mutator of your object for each attribute.
                                System.out.print("Edit your name :");
                                String name = inEdit.nextLine();
                                if (tutor!=null) {
                                    for (Tutor t : editTutors.getTutor()) {
                                            if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                t.setName(name);  
                                            }
                                        }
                                    bapp.updateTutors(editTutors, tapp.getFilePath());
                                } else if (student!=null) {
                                    for (Student s : editStudents.getStudent()) {
                                        if (student.getEmail().equals(s.getEmail()) && student.getPassword().equals(s.getPassword())) {
                                            s.setName(name);
                                        }
                                    
                                    }
                                    bapp.updateStudents(editStudents, sapp.getFilePath());
                                }
                                menu(tutor);
                                break;
                            case 2:
                                System.out.print("Edit your email:");
                                String email = inEdit.nextLine();
                                if (tutor!=null) {
                                    for (Tutor t : editTutors.getTutor()) {
                                            if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                t.setEmail(email);  
                                            }
                                        }
                                    bapp.updateTutors(editTutors, tapp.getFilePath());
                                } else if (student!=null) {
                                    for (Student s : editStudents.getStudent()) {
                                        if (student.getEmail().equals(s.getEmail()) && student.getPassword().equals(s.getPassword())) {
                                            s.setEmail(email);
                                        }
                                    
                                    }
                                    bapp.updateStudents(editStudents, sapp.getFilePath());
                                }
                                menu(tutor);
                                break;
                            case 3:
                                System.out.print("Edit your password:");
                                String pass = inEdit.nextLine();
                                if (tutor!=null) {
                                    for (Tutor t : editTutors.getTutor()) {
                                            if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                t.setPassword(pass);  
                                            }
                                        }
                                    bapp.updateTutors(editTutors, tapp.getFilePath());
                                } else if (student!=null) {
                                    for (Student s : editStudents.getStudent()) {
                                        if (student.getEmail().equals(s.getEmail()) && student.getPassword().equals(s.getPassword())) {
                                            s.setPassword(pass);
                                        }
                                    
                                    }
                                    bapp.updateStudents(editStudents, sapp.getFilePath());
                                }
                                menu(tutor);
                                break;
                            case 4:
                                System.out.print("Edit your DOB (xx-xx-xxxx format)");
                                String dob = inEdit.nextLine();
                                if (tutor!=null) {
                                    for (Tutor t : editTutors.getTutor()) {
                                            if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                t.setDob(dob);  
                                            }
                                        }
                                    bapp.updateTutors(editTutors, tapp.getFilePath());
                                } else if (student!=null) {
                                    for (Student s : editStudents.getStudent()) {
                                        if (student.getEmail().equals(s.getEmail()) && student.getPassword().equals(s.getPassword())) {
                                            s.setDob(dob);
                                        }
                                    
                                    }
                                    bapp.updateStudents(editStudents, sapp.getFilePath());
                                }
                                menu(tutor);
                                break;
                            case 5:
                                if (student!=null) {
                                    System.out.println("Invalid input, please try again");
                                    break;
                                } else if (tutor!=null) {
                                    System.out.print("Edit your subject (WSD, USP, SEP, AppProg, MobileApp:");
                                    String subject = inEdit.nextLine();
                                    if (tutor!=null) {
                                        for (Tutor t : editTutors.getTutor()) {
                                                if (tutor.getEmail().equals(t.getEmail()) && tutor.getPassword().equals(t.getPassword())) {
                                                    t.setSubject(subject);  
                                                }
                                            }
                                        bapp.updateTutors(editTutors, tapp.getFilePath());
                                    }
                                }
                                menu(tutor);
                                break;
                            case 0:
                                menu(tutor);
                                break;
                            default:
                                System.out.println("Invalid input, please try again");
                                menu(tutor);
                                break;
                        }

                        break;
                    case 6:
                        //Students cannot complete bookings, so show the same message as default then return to menu.
                        Bookings completeSearch = bapp.displayBookings();
                        if (student!=null) {
                            System.out.println("Invalid option, please select again");
                            menu(tutor);
                        } else if (tutor!=null) {
                            System.out.println("Which booking would you like to complete");
                            int book = in.nextInt();
                            //Iterate through bookings until there is a match with ID, then set the status to Completed then marshal it back to xml.
                            for (Booking b : completeSearch.getBooking()) {
                                if (book==b.getId()) {
                                    b.setStatus("Completed");
                                    System.out.println("Booking completed");
                                    System.out.println();
                                    bapp.updateBookings((completeSearch), bapp.getFilePath());
                                    break;
                                }
                            }
                            System.out.println("Booking ID not found");
                            System.out.println();
                            menu(tutor);
                            break;
                        }
                        menu(tutor);
                        break;
                    case 0:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option, please select again");
                        menu(tutor);
                        break;
                }
            }
        }
    }
        public static void menu(Tutor t) {
        System.out.println("Please select an option");
        System.out.println("1. View bookings");
        System.out.println("2. Create a booking");
        System.out.println("3. Cancel a booking");
        System.out.println("4. Cancel account");
        System.out.println("5. Edit account");
        if (t!=null) {
            System.out.println("6. Complete a booking");
        }
        System.out.println("0. Exit application");
    }
}
