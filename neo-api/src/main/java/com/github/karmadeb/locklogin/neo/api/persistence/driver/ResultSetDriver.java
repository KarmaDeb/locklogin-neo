package com.github.karmadeb.locklogin.neo.api.persistence.driver;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * The result set driver adds support for almost any common driver
 * compatible with java.
 */
public interface ResultSetDriver extends PersistenceDriver<Connection, ResultSet> {


}
