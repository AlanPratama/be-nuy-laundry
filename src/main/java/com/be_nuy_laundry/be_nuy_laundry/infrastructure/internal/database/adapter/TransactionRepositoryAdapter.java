package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.TransactionMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public TransactionModel save(TransactionModel transactionModel) {
        TransactionEntity entity = TransactionMapper.mapToEntity(transactionModel);

        TransactionEntity savedEntity = transactionJpaRepository.save(entity);

        return TransactionMapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<TransactionModel> findById(String id) {
        return transactionJpaRepository.findById(id).map(TransactionMapper::mapToDomain);
    }

    @Override
    public Page<TransactionModel> findAll(TransactionFilter transactionFilter, Pageable pageable) {
        Specification<TransactionEntity> specification = TransactionSpecification.getFilterSpecification(transactionFilter);

        return transactionJpaRepository.findAll(specification, pageable).map(TransactionMapper::mapToDomain);
    }

    @Override
    public String getInvoiceNo(String id) {
        return transactionJpaRepository.getInvoiceNo(id);
    }

    @Override
    public void deleteById(String id) {
        transactionJpaRepository.deleteById(id);
    }
}
