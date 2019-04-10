package com.algonquincollege.cst8277.models;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-03-16T20:20:07.782+0000")
@StaticMetamodel(Phone.class)
public class Phone_ extends ModelBase_ {
	public static volatile SingularAttribute<Phone, String> areaCode;
	public static volatile SingularAttribute<Phone, String> phoneNumber;
	public static volatile SingularAttribute<Phone, String> type;
	public static volatile SingularAttribute<Phone, Employee> owner;
}
