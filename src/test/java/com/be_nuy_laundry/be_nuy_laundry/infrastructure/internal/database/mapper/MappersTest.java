package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.*;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionFileType;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.UserSessionResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MappersTest {

    @Test
    @DisplayName("CustomerMapper - All Mapping Methods")
    void testCustomerMapper() {
        new CustomerMapper();

        assertThat(CustomerMapper.mapToEntity(null)).isNull();
        assertThat(CustomerMapper.mapToDomain(null)).isNull();
        assertThat(CustomerMapper.mapToEntityList(null)).isEmpty();
        assertThat(CustomerMapper.mapToDomainList(null)).isEmpty();

        CustomerModel model = TestDataBuilder.createSampleCustomer();
        CustomerEntity entity = CustomerMapper.mapToEntity(model);
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(model.getName());

        CustomerModel domain = CustomerMapper.mapToDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo(entity.getName());

        assertThat(CustomerMapper.mapToEntityList(List.of(model))).hasSize(1);
        assertThat(CustomerMapper.mapToDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("UserMapper - All Mapping Methods")
    void testUserMapper() {
        new UserMapper();

        assertThat(UserMapper.mapToEntity((UserModel) null)).isNull();
        assertThat(UserMapper.mapToEntity((List<UserModel>) null)).isEmpty();
        assertThat(UserMapper.mapToDomain(null)).isNull();
        assertThat(UserMapper.mapToDomainList(null)).isEmpty();
        assertThat(UserMapper.mapEntityToResponse(null)).isNull();
        assertThat(UserMapper.mapEntityToResponseList(null)).isEmpty();
        assertThat(UserMapper.mapResponseToEntity(null)).isNull();
        assertThat(UserMapper.mapResponseToEntityList(null)).isEmpty();
        assertThat(UserMapper.mapDomainToResponse(null)).isNull();
        assertThat(UserMapper.mapDomainToResponseList((List<UserModel>) null)).isEmpty();

        UserModel model = TestDataBuilder.createSampleUser();
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        model.setTransactions(List.of(trx));

        UserEntity entity = UserMapper.mapToEntity(model);
        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isEqualTo(model.getUsername());
        assertThat(entity.getTransactions()).hasSize(1);

        UserModel domain = UserMapper.mapToDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.getUsername()).isEqualTo(entity.getUsername());
        assertThat(domain.getTransactions()).hasSize(1);

        UserResponseDTO responseDTO = UserMapper.mapDomainToResponse(model);
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getUsername()).isEqualTo(model.getUsername());
        assertThat(responseDTO.getTransactions()).hasSize(1);

        UserResponseDTO entityToResp = UserMapper.mapEntityToResponse(entity);
        assertThat(entityToResp).isNotNull();
        assertThat(entityToResp.getTransactions()).hasSize(1);

        UserEntity respToEntity = UserMapper.mapResponseToEntity(responseDTO);
        assertThat(respToEntity).isNotNull();
        assertThat(respToEntity.getTransactions()).hasSize(1);

        assertThat(UserMapper.mapToEntity(List.of(model))).hasSize(1);
        assertThat(UserMapper.mapToDomainList(List.of(entity))).hasSize(1);
        assertThat(UserMapper.mapEntityToResponseList(List.of(entity))).hasSize(1);
        assertThat(UserMapper.mapResponseToEntityList(List.of(responseDTO))).hasSize(1);
        assertThat(UserMapper.mapDomainToResponseList(List.of(model))).hasSize(1);
    }

    @Test
    @DisplayName("PackageMapper & BenefitMapper - All Mapping Methods")
    void testPackageAndBenefitMapper() {
        new PackageMapper();
        new BenefitMapper();

        assertThat(PackageMapper.mapToEntity(null)).isNull();
        assertThat(PackageMapper.mapToDomain(null)).isNull();
        assertThat(PackageMapper.mapToEntityList(null)).isEmpty();
        assertThat(PackageMapper.mapToDomainList(null)).isEmpty();

        PackageModel pkgModel = TestDataBuilder.createSamplePackage();
        PackageEntity pkgEntity = PackageMapper.mapToEntity(pkgModel);
        assertThat(pkgEntity).isNotNull();
        assertThat(pkgEntity.getName()).isEqualTo(pkgModel.getName());

        PackageModel pkgDomain = PackageMapper.mapToDomain(pkgEntity);
        assertThat(pkgDomain).isNotNull();
        assertThat(pkgDomain.getName()).isEqualTo(pkgEntity.getName());

        assertThat(PackageMapper.mapToEntityList(List.of(pkgModel))).hasSize(1);
        assertThat(PackageMapper.mapToDomainList(List.of(pkgEntity))).hasSize(1);

        // BenefitMapper
        assertThat(BenefitMapper.mapToEntity(null)).isNull();
        assertThat(BenefitMapper.mapToDomain(null)).isNull();
        assertThat(BenefitMapper.mapToEntityList(null)).isEmpty();
        assertThat(BenefitMapper.mapToDomainList(null)).isEmpty();

        BenefitModel benModel = TestDataBuilder.createSampleBenefit();
        BenefitEntity benEntity = BenefitMapper.mapToEntity(benModel);
        assertThat(benEntity).isNotNull();
        assertThat(benEntity.getName()).isEqualTo(benModel.getName());

        BenefitModel benDomain = BenefitMapper.mapToDomain(benEntity);
        assertThat(benDomain).isNotNull();
        assertThat(benDomain.getName()).isEqualTo(benEntity.getName());

        // Test non-empty list conversion with values for BenefitMapper
        assertThat(BenefitMapper.mapToEntityList(List.of(benModel))).hasSize(1);
        assertThat(BenefitMapper.mapToDomainList(List.of(benEntity))).hasSize(1);

        // PackageBenefitMapper
        new PackageBenefitMapper();
        assertThat(PackageBenefitMapper.getBenefitModelList(null)).isEmpty();

        PackageBenefitEntity pbe = new PackageBenefitEntity();
        pbe.setBenefit(benEntity);
        List<BenefitModel> mappedBenefits = PackageBenefitMapper.getBenefitModelList(List.of(pbe));
        assertThat(mappedBenefits).hasSize(1);
    }

    @Test
    @DisplayName("TransactionMapper & Details & Files - All Mapping Methods")
    void testTransactionMappers() {
        new TransactionMapper();
        new TransactionDetailMapper();
        new TransactionFileMapper();

        // TransactionMapper
        assertThat(TransactionMapper.mapToEntity(null)).isNull();
        assertThat(TransactionMapper.mapToDomain(null)).isNull();
        assertThat(TransactionMapper.mapDomainToResponse(null)).isNull();
        assertThat(TransactionMapper.mapToEntityList(null)).isEmpty();
        assertThat(TransactionMapper.mapToDomainList(null)).isEmpty();
        assertThat(TransactionMapper.mapDomainToResponseList(null)).isEmpty();

        TransactionModel trxModel = TestDataBuilder.createSampleTransaction();
        TransactionFileModel fileModel = TestDataBuilder.createSampleTransactionFile();
        trxModel.setTransactionFiles(List.of(fileModel));

        TransactionEntity trxEntity = TransactionMapper.mapToEntity(trxModel);
        assertThat(trxEntity).isNotNull();
        assertThat(trxEntity.getInvoiceNo()).isEqualTo(trxModel.getInvoiceNo());

        TransactionModel trxDomain = TransactionMapper.mapToDomain(trxEntity);
        assertThat(trxDomain).isNotNull();
        assertThat(trxDomain.getInvoiceNo()).isEqualTo(trxEntity.getInvoiceNo());

        TransactionResponseDTO trxResp = TransactionMapper.mapDomainToResponse(trxModel);
        assertThat(trxResp).isNotNull();

        assertThat(TransactionMapper.mapToEntityList(List.of(trxModel))).hasSize(1);
        assertThat(TransactionMapper.mapToDomainList(List.of(trxEntity))).hasSize(1);
        assertThat(TransactionMapper.mapDomainToResponseList(List.of(trxModel))).hasSize(1);

        // TransactionDetailMapper
        assertThat(TransactionDetailMapper.mapToEntity(null)).isNull();
        assertThat(TransactionDetailMapper.mapToDomain(null)).isNull();
        assertThat(TransactionDetailMapper.mapToEntityList(null)).isEmpty();
        assertThat(TransactionDetailMapper.mapToDomainList(null)).isEmpty();

        TransactionDetailModel detailModel = trxModel.getTransactionDetails().get(0);
        TransactionDetailEntity detailEntity = TransactionDetailMapper.mapToEntity(detailModel);
        assertThat(detailEntity).isNotNull();

        PackageEntity pkgEntity = new PackageEntity();
        pkgEntity.setId("pkg-123");
        detailEntity.setPkg(pkgEntity);
        detailEntity.setTransaction(trxEntity);

        TransactionDetailModel detailDomain = TransactionDetailMapper.mapToDomain(detailEntity);
        assertThat(detailDomain).isNotNull();
        assertThat(detailDomain.getPackageId()).isEqualTo("pkg-123");
        assertThat(detailDomain.getTransactionId()).isEqualTo(trxEntity.getId());

        assertThat(TransactionDetailMapper.mapToEntityList(List.of(detailModel))).hasSize(1);
        assertThat(TransactionDetailMapper.mapToDomainList(List.of(detailEntity))).hasSize(1);

        // TransactionFileMapper
        assertThat(TransactionFileMapper.mapToEntity(null)).isNull();
        assertThat(TransactionFileMapper.mapToDomain(null)).isNull();
        assertThat(TransactionFileMapper.mapToEntityList(null)).isEmpty();
        assertThat(TransactionFileMapper.mapToDomainList(null)).isEmpty();

        TransactionFileEntity fileEntity = TransactionFileMapper.mapToEntity(fileModel);
        assertThat(fileEntity).isNotNull();

        TransactionFileModel fileDomain = TransactionFileMapper.mapToDomain(fileEntity);
        assertThat(fileDomain).isNotNull();

        assertThat(TransactionFileMapper.mapToEntityList(List.of(fileModel))).hasSize(1);
        assertThat(TransactionFileMapper.mapToDomainList(List.of(fileEntity))).hasSize(1);

        assertThat(TransactionFileMapper.mapMultipartFileToDomain(null, TransactionFileType.PAYMENT_RECEIPT, "path")).isNull();

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        TransactionFileModel multipartModel = TransactionFileMapper.mapMultipartFileToDomain(
                mockFile, TransactionFileType.CONDITION_BEFORE, "/path/test.jpg");
        assertThat(multipartModel).isNotNull();
        assertThat(multipartModel.getFileName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("UserSessionMapper - All Mapping Methods")
    void testUserSessionMapper() {
        new UserSessionMapper();

        assertThat(UserSessionMapper.mapToEntity(null)).isNull();
        assertThat(UserSessionMapper.mapToDomain(null)).isNull();
        assertThat(UserSessionMapper.mapDomainToResponse(null)).isNull();
        assertThat(UserSessionMapper.mapEntityToResponse(null)).isNull();
        assertThat(UserSessionMapper.mapDomainToAuthResponse(null)).isNull();
        assertThat(UserSessionMapper.mapToEntityList(null)).isEmpty();
        assertThat(UserSessionMapper.mapToDomainList(null)).isEmpty();
        assertThat(UserSessionMapper.mapDomainToResponseList(null)).isEmpty();
        assertThat(UserSessionMapper.mapEntityToResponseList(null)).isEmpty();
        assertThat(UserSessionMapper.mapDomainToAuthResponseList(null)).isEmpty();

        UserSessionModel sessionModel = TestDataBuilder.createSampleSession();
        UserSessionEntity sessionEntity = UserSessionMapper.mapToEntity(sessionModel);
        assertThat(sessionEntity).isNotNull();

        UserSessionModel sessionDomain = UserSessionMapper.mapToDomain(sessionEntity);
        assertThat(sessionDomain).isNotNull();

        UserSessionResponseDTO sessionResp = UserSessionMapper.mapDomainToResponse(sessionModel);
        assertThat(sessionResp).isNotNull();

        UserSessionResponseDTO entityResp = UserSessionMapper.mapEntityToResponse(sessionEntity);
        assertThat(entityResp).isNotNull();

        AuthResponseDTO authResp = UserSessionMapper.mapDomainToAuthResponse(sessionModel);
        assertThat(authResp).isNotNull();
        assertThat(authResp.getAccessToken()).isEqualTo(sessionModel.getAccessToken());

        assertThat(UserSessionMapper.mapToEntityList(List.of(sessionModel))).hasSize(1);
        assertThat(UserSessionMapper.mapToDomainList(List.of(sessionEntity))).hasSize(1);
        assertThat(UserSessionMapper.mapDomainToResponseList(List.of(sessionModel))).hasSize(1);
        assertThat(UserSessionMapper.mapEntityToResponseList(List.of(sessionEntity))).hasSize(1);
        assertThat(UserSessionMapper.mapDomainToAuthResponseList(List.of(sessionModel))).hasSize(1);
    }
}
