package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionFileRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionFileEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.TransactionFileMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionFileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionFileRepositoryAdapter implements TransactionFileRepositoryPort {

    private final TransactionFileJpaRepository transactionFileJpaRepository;

    @Override
    public Optional<TransactionFileModel> findById(String id) {
        return transactionFileJpaRepository.findById(id)
                .map(TransactionFileMapper::mapToDomain);
    }

    @Override
    public List<TransactionFileModel> findByIds(List<String> ids) {
        return TransactionFileMapper.mapToDomainList(
                transactionFileJpaRepository.findAllById(ids));
    }

    @Override
    public List<TransactionFileModel> saveAll(List<TransactionFileModel> transactionFileModels) {
        List<TransactionFileEntity> transactionFileEntities =
                TransactionFileMapper.mapToEntityList(transactionFileModels);

        return TransactionFileMapper.mapToDomainList(
                transactionFileJpaRepository.saveAll(transactionFileEntities));
    }

    @Override
    public void deleteById(String id) {
        transactionFileJpaRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        transactionFileJpaRepository.deleteByIds(ids);
    }
}
