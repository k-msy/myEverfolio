package entity;

import entity.UserGroup;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-12-11T06:40:55")
@StaticMetamodel(Users.class)
public class Users_ { 

    public static volatile SingularAttribute<Users, String> pw;
    public static volatile SingularAttribute<Users, String> name;
    public static volatile SingularAttribute<Users, String> id;
    public static volatile SingularAttribute<Users, UserGroup> group;

}