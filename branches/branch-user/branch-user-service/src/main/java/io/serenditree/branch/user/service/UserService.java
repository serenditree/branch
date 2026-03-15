package io.serenditree.branch.user.service;


import io.serenditree.branch.user.model.entities.Role;
import io.serenditree.branch.user.model.entities.User;
import io.serenditree.branch.user.repository.api.UserRepositoryApi;
import io.serenditree.branch.user.service.api.UserServiceApi;
import io.serenditree.branch.user.wind.api.OutgoingWindApi;
import io.serenditree.fence.authentication.service.api.PasswordServiceApi;
import io.serenditree.fence.model.FenceResponse;
import io.serenditree.fence.model.Principal;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.util.maple.Maple;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@Dependent
public class UserService implements UserServiceApi {

    private UserRepositoryApi userRepository;

    private PasswordServiceApi passwordService;

    private OutgoingWindApi outgoingWind;

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public User retrieveByUsername(final String username) {
        return this.userRepository.retrieveByUsername(username);
    }

    @Override
    public List<User> retrieveBySubstring(final String substring) {
        return this.userRepository.retrieveBySubstring(substring);
    }

    @Override
    public FenceResponse delete(final Long id, final boolean includeContributions) {
        if (this.userRepository.deleteById(id) && includeContributions) {
            this.outgoingWind.releaseUserDeleted(id);
        }

        return new FenceResponse(id);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // AUTH
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public FencePrincipal signUp(FencePrincipal principal) {

        User user = new User(
            principal.getUsername(),
            this.passwordService.hash(principal.getPassword()),
            principal.getEmail()
        );

        user.addRole(RoleType.USER);

        return this.userToPrincipal(this.userRepository.create(user));
    }

    @Override
    public FencePrincipal retrievePrincipalByUsername(String username) {

        return this.userToPrincipal(this.userRepository.retrieveByUsername(username));
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void verify(FencePrincipal principal) {
        this.userRepository.verify(principal);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: AUTH
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private FencePrincipal userToPrincipal(final User user) {
        FencePrincipal principal = null;

        if (user != null) {
            principal = new Principal(user.getId(), user.getUsername(), user.getPassword(), null, user.getEmail());
            principal.setRoleTypes(Maple.mapSetToList(user.getRoles(), Role::getRoleType));
        }

        return principal;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setUserRepository(UserRepositoryApi userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    public void setPasswordService(PasswordServiceApi passwordService) {
        this.passwordService = passwordService;
    }

    @Inject
    public void setOutgoingWind(OutgoingWindApi outgoingWind) {
        this.outgoingWind = outgoingWind;
    }
}
