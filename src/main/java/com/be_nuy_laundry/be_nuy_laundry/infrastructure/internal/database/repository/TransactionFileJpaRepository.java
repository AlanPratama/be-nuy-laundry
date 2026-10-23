package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionFileJpaRepository extends JpaRepository<TransactionFileEntity, String> {

    @Modifying
    @Query(value = "DELETE FROM transaction_files WHERE id IN (:ids)", nativeQuery = true)
    void deleteByIds(@Param("ids")List<String> ids);

}
