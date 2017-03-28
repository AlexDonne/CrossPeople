package com.example.extra.socialproject.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Création d'un utilisateur
 */
@IgnoreExtraProperties
public class User implements Serializable, Parcelable{
    private String id;
    private String name;
    private String linkURI;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLinkURI() {
        return linkURI;
    }
    @SuppressWarnings("unused") public void setLinkURI(String linkURI) { this.linkURI = linkURI; }

    /**
     * Création d'un utilisateur avec son id, son nom et son lien de profil facebook
     * @param id Id de l'utilisateur
     * @param name Nom de l'utilisateur
     * @param linkURI Lien du profil facebook de l'utilisateur
     */
    public User (String id, String name, String linkURI){
        this.id=id;
        this.name=name;
        this.linkURI = linkURI;
    }

    /**
     * Création d'un utilisateur "complexe" avec Parcelable
     * @param in Contient les arguments de l'utilisateur
     */
    private User (Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.linkURI = in.readString();
    }

    @SuppressWarnings("unused")
    public User(){

    }

    @Override
    public boolean equals (Object o){
        if (o instanceof User) {
            User u= (User) o;
            if (u.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.linkURI);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
