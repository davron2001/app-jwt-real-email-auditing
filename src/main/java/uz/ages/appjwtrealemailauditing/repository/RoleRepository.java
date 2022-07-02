package uz.ages.appjwtrealemailauditing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.ages.appjwtrealemailauditing.entity.Role;
import uz.ages.appjwtrealemailauditing.entity.enums.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(RoleName roleName);
}
