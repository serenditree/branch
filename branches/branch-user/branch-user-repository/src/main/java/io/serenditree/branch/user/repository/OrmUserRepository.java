package io.serenditree.branch.user.repository;


import io.serenditree.branch.user.model.entities.User;
import io.serenditree.branch.user.repository.api.UserRepository;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ValidationException;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.List;

@Dependent
@Retry(
    maxRetries = 4,
    delay = 420L,
    abortOn = {
        PersistenceException.class,
        ValidationException.class
    }
)
public class OrmUserRepository implements UserRepository {

    @Override
    public User create(User user) {
        this.getEntityManager().persist(user);

        return user;
    }

    @Override
    public void verify(FencePrincipal principal) {
        User userReference = this.getEntityManager().getReference(User.class, principal.getId());
        userReference.addRole(RoleType.HUMAN);
        this.getEntityManager().persist(userReference);
    }

    @Override
    public User retrieveByUsername(String username) {
        return this.getEntityManager().createNamedQuery(User.RETRIEVE_BY_USERNAME, User.class)
            .setParameter(User.USERNAME_REFERENCE, username)
            .getSingleResult();
    }

    @Override
    public List<User> retrieveBySubstring(String substring) {
        return this.getEntityManager().createNamedQuery(User.RETRIEVE_BY_SUBSTRING, User.class)
            .setParameter(User.SUBSTRING_REFERENCE, substring.toLowerCase() + "%")
            .getResultList();
    }

    @Override
    public boolean deleteById(Long id) {
        return this.getEntityManager().createNamedQuery(User.DELETE_BY_ID)
                   .setParameter(User.ID_REFERENCE, id)
                   .executeUpdate() > 0;
    }
}
