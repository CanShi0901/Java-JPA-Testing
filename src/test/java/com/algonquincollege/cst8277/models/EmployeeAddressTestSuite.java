/**
 * File: EmployeeAddressTestSuite.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu 040885680
 * (Modified) @date: 2019 03 13
 * (Professor) @author Mike Norman
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.TestSuiteConstants.buildEntityManagerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.invoke.MethodHandles;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeAddressTestSuite implements TestSuiteConstants {

    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LoggerFactory.getLogger(_thisClaz);
    private static final ch.qos.logback.classic.Logger eclipselinkSqlLogger =
        (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ECLIPSELINK_LOGGING_SQL);

    // test fixture(s)
    public static EntityManagerFactory emf;
    public static Server server;

    @BeforeClass
    public static void oneTimeSetUp() {
        try {
            logger.debug("oneTimeSetUp");
            // create in-process H2 server so we can 'see' into database
            // use "jdbc:h2:tcp://localhost:9092/mem:assignment3-testing" in Db Perspective
            // (connection in .dbeaver-data-sources.xml so should be immediately useable
            server = Server.createTcpServer().start();
            emf = buildEntityManagerFactory(_thisClaz.getSimpleName());
        }
        catch (Exception e) {
            logger.error("something went wrong building EntityManagerFactory", e);
        }
    }

    /**
     * adding a new address, test it can be found
     */
    @Test
    public void test00_add_address() {
        
        EntityManager em = emf.createEntityManager();
        Address address = new Address();
        address.setStreet("530 Boul.St-Joseph");
        address.setCity("Gatineau");
        address.setPostal("J8Y 4A3");
        address.setState("QC");
        address.setCountry("Canada");
        address.setVersion(1);
        em.getTransaction().begin();
        em.persist(address);
        em.getTransaction().commit();
        assertNotNull(em.find(Address.class, 3)); 
        em.close();
    }
    
    /**
     * add an employee and give the address to this employee
     */
    @Test
    public void test01_add_employee_with_persisted_address() {
        
        EntityManager em = emf.createEntityManager();
        Employee e = new Employee();
        e.setFirstName("Mike");
        e.setLastName("Stevenson");
        e.setSalary(50000);
        e.setAddress(em.find(Address.class, 3));
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertNotNull(em.find(Employee.class, 2));
        em.close();
    }
    
    /**
     * address not persisited, test for rollback exception 
     */
    @Test(expected=javax.persistence.RollbackException.class)
    public void test02_add_employee_with_not_persisted_address() {
        
        EntityManager em = emf.createEntityManager();
        
        Address a = new Address();
        a.setStreet("1399 Bank");
        a.setCity("Ottawa");
        a.setState("ON");
        a.setCountry("Canada");
        a.setPostal("K1A 2B3");
        a.setVersion(1);
        
        Employee e = new Employee();
        e.setFirstName("Mike");
        e.setLastName("Stevenson");
        e.setSalary(50000);
        e.setAddress(a);
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        em.close();
    }
    
    /**
     * test cannot add a new employee without adding address
     */
    @Test
    public void test03_add_employee_without_address() {
        
        EntityManager em = emf.createEntityManager();
        Employee e = new Employee();
        e.setFirstName("Victor");
        e.setLastName("Joly");
        e.setSalary(80000);
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertNull(em.find(Employee.class, 4));
        em.close();
    } 
    
    /**
     * test delete an address associated with an employee using jpql
     */
    @Test(expected = javax.persistence.PersistenceException.class)
    public void test04_delete_address_with_employee() {
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Address where id = 3").executeUpdate(); 
        em.getTransaction().commit();
    }
    
    /**
     * test delete an address not associated with an employee
     */
    @Test
    public void test05_delete_address_without_employee() {
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int deletedCount = em.createQuery("DELETE FROM Address where id = 2").executeUpdate(); 
        em.getTransaction().commit();
        assertNull(em.find(Address.class, 2));
        assertEquals(1, deletedCount);
        em.close();
    }       

    /**
     * test delete an employee associated with an address
     */
    @Test
    public void test06_delete_employee_with_address() {
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int deletedCount = em.createQuery("DELETE FROM Employee where id = 2").executeUpdate(); 
        em.getTransaction().commit();
        assertNull(em.find(Employee.class, 2));
        assertEquals(1, deletedCount);
        em.close();
    }
    
    /**
     * find address by employee
     */
    @Test
    public void test07_find_address_by_employee() {
        
        EntityManager em = emf.createEntityManager();
        int add_id = em.find(Employee.class, 1).getAddress().getId();
        Address a = em.find(Address.class, add_id);
        assertEquals(a, em.find(Address.class, 1));
        em.close();
    }

    /**
     * find employee using address
     */
    @Test
    public void test08_find_employee_by_address() {
        
        EntityManager em = emf.createEntityManager();
        int addr_id = em.find(Address.class, 1).getId();
        Employee e = em.createQuery("SELECT e FROM Employee e where e.address.id = ?1", Employee.class)
                .setParameter(1,addr_id)
                .getSingleResult();
        assertEquals(e, em.find(Employee.class, 1));
        em.close();
    }   

    /**
     * test one address cannot be added to two employees
     */
    @Test(expected=javax.persistence.RollbackException.class)
    public void test09_one_address_for_two_employee() {
        
        EntityManager em = emf.createEntityManager();
        Employee e = new Employee();
        e.setFirstName("Gilles");
        e.setLastName("Hamel");
        e.setSalary(80000);
        e.setAddress(em.find(Address.class, 1));
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        em.close();
    }
    
    /**
     * test update address not associated with employee
     */
    @Test
    public void test10_update_address_without_employee() {
        EntityManager em = emf.createEntityManager();
        int add_id = em.find(Employee.class, 1).getAddress().getId();
        Address a = em.find(Address.class, add_id);
        a.setCity("Montreal");
        em.persist(a);    
        assertEquals("Montreal", em.find(Address.class, add_id).getCity());
        em.close();
    }
    
    /**
     * test updaste address associated with employee
     */
    @Test
    public void test11_update_address_with_employee() {
              
        EntityManager em = emf.createEntityManager();
        Address a = new Address();
        a.setStreet("140 Promenade du Portage");
        a.setCity("Ottawa");
        a.setState("ON");
        a.setCountry("Canada");
        a.setPostal("J8T 3X8");
        a.setVersion(1);
        em.getTransaction().begin();
        em.persist(a);
        em.getTransaction().commit();
        Address addr = em.find(Address.class, 4);
        a.setState("QC");
        em.persist(addr);    
        assertEquals("QC", em.find(Address.class, 4).getState());
        em.close();
    } 
    
    /**
     * test update employee not persisted address
     */
    @Test(expected=javax.persistence.PersistenceException.class)
    public void test12_update_employee_with_not_persisted_address() {
              
        EntityManager em = emf.createEntityManager();
        Address a = new Address();
        a.setStreet("1350 Woodroffe");
        a.setCity("Ottawa");
        a.setState("ON");
        a.setCountry("Canada");
        a.setPostal("K8X 1Y3");
        a.setVersion(1);

        //Creteria update
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<Employee> update = cb.createCriteriaUpdate(Employee.class);
        Root<Employee> e = update.from(Employee.class);
        update.set("address", a);
        update.where(cb.equal(e.get("id"), 1));
        em.getTransaction().begin();
        em.createQuery(update).executeUpdate();
        em.getTransaction().commit();

        em.close();
    } 
    
    @AfterClass
    public static void oneTimeTearDown() {
        logger.debug("oneTimeTearDown");
        if (emf != null) {
            emf.close();
        }
        if (server != null) {
            server.stop();
        }
    }

}