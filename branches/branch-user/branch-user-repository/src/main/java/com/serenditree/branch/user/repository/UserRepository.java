package com.serenditree.branch.user.repository;


import com.serenditree.branch.user.model.entities.User;
import com.serenditree.branch.user.repository.api.UserRepositoryApi;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.List;

@Dependent
@Retry(
    maxRetries = 4,
    delay = 420L,
    abortOn = {
        EntityExistsException.class,
        EntityNotFoundException.class,
        NonUniqueResultException.class,
        NoResultException.class
    }
)
public class UserRepository implements UserRepositoryApi {

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
            .setParameter(User.SUBSTRING_REFERENCE, substring + "%")
            .getResultList();
    }
}
