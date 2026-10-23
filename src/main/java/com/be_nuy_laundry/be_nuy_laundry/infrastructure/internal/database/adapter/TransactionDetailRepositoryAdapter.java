package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionDetailRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.TransactionDetailMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionDetailJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionDetailRepositoryAdapter implements TransactionDetailRepositoryPort {

    private final TransactionDetailJpaRepository transactionDetailJpaRepository;

    @Override
    public List<TransactionDetailModel> findByTransactionId(String transactionId) {
        return TransactionDetailMapper.mapToDomainList(transactionDetailJpaRepository.findByTransactionId(transactionId));
    }

    @Override
    public Optional<TransactionDetailModel> findById(String id) {
        return transactionDetailJpaRepository.findById(id).map(TransactionDetailMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        transactionDetailJpaRepository.deleteById(id);
    }
}
