package be.crydust.fetchurl;

import java.lang.reflect.Field;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * There are a couple of commonly quoted solutions to this problem.
 *
 * Unfortunately neither of these are entirely satisfactory:
 * <ul><li>
 * Install the unlimited strength policy files. While this is probably the right
 * solution for your development workstation, it quickly becomes a major hassle
 * (if not a roadblock) to have non-technical users install the files on every
 * computer. There is no way to distribute the files with your program; they
 * must be installed in the JRE directory (which may even be read-only due to
 * permissions)
 * </li><li>
 * Skip the JCE API and use another cryptography library such as Bouncy Castle.
 * This approach requires an extra 1MB library, which may be a significant
 * burden depending on the application. It also feels silly to duplicate
 * functionality included in the standard libraries. Obviously, the API is also
 * completely different from the usual JCE interface. (BC does implement a JCE
 * provider, but that doesn't help because the key strength restrictions are
 * applied before handing over to the implementation.) This solution also won't
 * let you use 256-bit TLS (SSL) cipher suites, because the standard TLS
 * libraries call the JCE internally to determine any restrictions.
 * </li><li>
 * But then there's reflection. Is there anything you can't do using reflection?
 * </li></ul>
 *
 * @see http://stackoverflow.com/a/22492582/11451
 * @author ntoskrnl
 */
public class CryptoHack {

    private static final Logger logger = Logger.getLogger(CryptoHack.class.getName());

    public static void removeCryptographyRestrictions() {
        if (!isRestrictedCryptography()) {
            logger.fine("Cryptography restrictions removal not needed");
            return;
        }
        try {
            /*
             * Do the following, but with reflection to bypass access checks:
             *
             * JceSecurity.isRestricted = false;
             * JceSecurity.defaultPolicy.perms.clear();
             * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
             */
            final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

            final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
            isRestrictedField.setAccessible(true);
            isRestrictedField.set(null, false);

            final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
            defaultPolicyField.setAccessible(true);
            final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

            final Field perms = cryptoPermissions.getDeclaredField("perms");
            perms.setAccessible(true);
            ((Map<?, ?>) perms.get(defaultPolicy)).clear();

            final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            defaultPolicy.add((Permission) instance.get(null));

            logger.fine("Successfully removed cryptography restrictions");
        } catch (final Exception e) {
            logger.log(Level.WARNING, "Failed to remove cryptography restrictions", e);
        }
    }

    public static boolean isRestrictedCryptography() {
        // This simply matches the Oracle JRE, but not OpenJDK.
        return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
    }
}
