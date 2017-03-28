package com.example.extra.socialproject.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour définir la création d'une salle de chat
 */
public class RoomCreator {
    private Map<String,Object> map = new HashMap<>();
    public Map getMapRoom(){
        return map;
    }

    /**
     * Création de la salle avec les 2 utilisateurs
     * @param user1 Nom de l'utilisateur 1
     * @param user2 Nom de l'utilisateur 2
     */
    public RoomCreator(String user1, String user2){
        map.put(user1+" - "+user2,"");
    }


}
