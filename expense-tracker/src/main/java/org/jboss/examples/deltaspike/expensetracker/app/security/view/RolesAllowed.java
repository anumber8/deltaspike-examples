package org.jboss.examples.deltaspike.expensetracker.app.security.view;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Stereotype;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.Secured;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.picketlink.Identity;
import org.picketlink.authorization.util.AuthorizationUtil;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;

@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
@Inherited
@Stereotype
@ViewMetaData // since 1.0.1
@Secured(RolesAllowed.Voter.class)
public @interface RolesAllowed {

    String[] value();

    @RequestScoped
    public static class Voter implements AccessDecisionVoter {

        @Inject
        private Identity identity;

        @Inject
        private PartitionManager partitionManager;

        @Inject
        private IdentityManager identityManager;

        @Inject
        private RelationshipManager relationshipManager;

        @Override
        public Set<SecurityViolation> checkPermission(AccessDecisionVoterContext accessDecisionVoterContext) {
            RolesAllowed metaData = accessDecisionVoterContext.getMetaDataFor(RolesAllowed.class.getName(), RolesAllowed.class);
            for (String role : metaData.value()) {
                if (AuthorizationUtil.hasRole(identity, partitionManager, identityManager, relationshipManager, role)) {
                    return Collections.<SecurityViolation>emptySet();
                }
            }
            return Collections.<SecurityViolation>singleton(new SecurityViolation() {
                @Override
                public String getReason() {
                    return "No user's role is authorized to access this resource.";
                }
            });
        }

    }

}
