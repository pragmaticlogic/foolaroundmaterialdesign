package com.codeprototype.kevin.foolaroundmaterialdesign.dbmodel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by kevin on 10/13/15.
 */
@Table(name = "Token")
public class Token extends Model {
    @Column(name = "SessionToken")
    public String sessionToken;

    public static Token getToken() {
        return new Select().from(Token.class).executeSingle();
    }
}
