package net.researchgate.azkaban;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import azkaban.user.User;
import azkaban.utils.Props;

public class LdapUserManagerIntegrationTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private LdapUserManager userManager;

	@Before
	public void setUp() throws Exception {
		Props props = getProps();
		userManager = new LdapUserManager(props);
	}

	private Props getProps() {
		Props props = new Props();
		props.put(LdapUserManager.LDAP_HOST, "ldap.forumsys.com");
		props.put(LdapUserManager.LDAP_PORT, "389");
		props.put(LdapUserManager.LDAP_USE_SSL, "false");
		props.put(LdapUserManager.LDAP_USER_BASE, "dc=example,dc=com");
		props.put(LdapUserManager.LDAP_USERID_PROPERTY, "uid");
		props.put(LdapUserManager.LDAP_EMAIL_PROPERTY, "mail");
		props.put(LdapUserManager.LDAP_BIND_ACCOUNT, "cn=read-only-admin,dc=example,dc=com");
		props.put(LdapUserManager.LDAP_BIND_PASSWORD, "password");
		props.put(LdapUserManager.LDAP_ALLOWED_GROUPS, "mathematicians");
		props.put(LdapUserManager.LDAP_GROUP_SEARCH_BASE, "dc=example,dc=com");
		props.put(LdapUserManager.LDAP_GROUPS_FILE, getClass().getResource("/azkaban-groups.xml").getFile());
		return props;
	}

	@Test
	public void testGetUser() throws Exception {
		User user = userManager.getUser("gauss", "password");

		assertEquals("gauss", user.getUserId());
		assertEquals("gauss@ldap.forumsys.com", user.getEmail());
	}

	@Test
	public void testValidateUser() throws Exception {
		assertTrue(userManager.validateUser("gauss"));
		assertFalse(userManager.validateUser("invalid"));
	}

	@Test
	public void testInvalidEmailPropertyDoesNotThrowNullPointerException() throws Exception {
		Props props = getProps();
		props.put(LdapUserManager.LDAP_EMAIL_PROPERTY, "invalidField");
		userManager = new LdapUserManager(props);
		User user = userManager.getUser("gauss", "password");

		assertEquals("gauss", user.getUserId());
		assertEquals("", user.getEmail());
	}

	@Test
	public void testUserIsMemberOfGroup() throws Exception {
		Props props = getProps();
		props.put(LdapUserManager.LDAP_ROLE_SUPPORT, "true");

		userManager = new LdapUserManager(props);

		User userRiemann = userManager.getUser("riemann", "password");
		User userEinstein = userManager.getUser("einstein", "password");

		assertTrue(userRiemann.getGroups().contains("mathematicians"));
		assertTrue(userEinstein.getGroups().contains("scientists"));
	}

	@Test
	public void testUserHasRoleAssigned() throws Exception {
		Props props = getProps();
		props.put(LdapUserManager.LDAP_ROLE_SUPPORT, "true");

		userManager = new LdapUserManager(props);

		User userRiemann = userManager.getUser("riemann", "password");
		User userEinstein = userManager.getUser("einstein", "password");

		assertTrue(userRiemann.hasRole("administrator"));

		assertTrue(userEinstein.hasRole("viewer"));
		assertTrue(userEinstein.hasRole("executor"));
	}
}
