package com.be_nuy_laundry.be_nuy_laundry.fixture;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionFileType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class TestDataBuilder {

    public static UserModel createSampleUser() {
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID().toString());
        user.setName("Test User");
        user.setUsername("testuser");
        user.setPassword("encodedPassword123");
        user.setRole(UserRole.ADMIN);
        user.setIsActive(true);
        user.setProfilePicture("/users/avatar.png");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static CustomerModel createSampleCustomer() {
        CustomerModel customer = new CustomerModel();
        customer.setId(UUID.randomUUID().toString());
        customer.setName("John Doe");
        customer.setPhone("08123456789");
        customer.setAddress("Jl. Sudirman No. 123");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setTransactions(new ArrayList<>());
        return customer;
    }

    public static PackageModel createSamplePackage() {
        PackageModel pkg = new PackageModel();
        pkg.setId(UUID.randomUUID().toString());
        pkg.setName("Cuci Komplit Kilat");
        pkg.setDescription("Cuci, kering, setrika selesai 1 hari");
        pkg.setPrice(BigDecimal.valueOf(15000));
        pkg.setType(PackageType.WEIGHT);
        pkg.setDaysEstimation(1);
        pkg.setPhoto("/packages/kilat.png");
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        return pkg;
    }

    public static BenefitModel createSampleBenefit() {
        BenefitModel benefit = new BenefitModel();
        benefit.setId(UUID.randomUUID().toString());
        benefit.setName("Parfum Premium");
        benefit.setCreatedAt(LocalDateTime.now());
        benefit.setUpdatedAt(LocalDateTime.now());
        return benefit;
    }

    public static TransactionModel createSampleTransaction() {
        UserModel user = createSampleUser();
        CustomerModel customer = createSampleCustomer();
        PackageModel pkg = createSamplePackage();

        TransactionDetailModel detail = new TransactionDetailModel();
        detail.setId(UUID.randomUUID().toString());
        detail.setPkg(pkg);
        detail.setQuantity(2);
        detail.setTotalPrice(BigDecimal.valueOf(30000));

        TransactionModel trx = new TransactionModel();
        trx.setId(UUID.randomUUID().toString());
        trx.setInvoiceNo("TRX-SEN-23102026-ABC123DEF456GH");
        trx.setStatus(TransactionStatus.IN_PROGRESS);
        trx.setTotalPrice(BigDecimal.valueOf(30000));
        trx.setUser(user);
        trx.setCustomer(customer);
        trx.setTransactionDetails(new ArrayList<>());
        trx.getTransactionDetails().add(detail);
        trx.setCreatedAt(LocalDateTime.now());
        trx.setUpdatedAt(LocalDateTime.now());
        return trx;
    }

    public static TransactionFileModel createSampleTransactionFile() {
        TransactionFileModel file = new TransactionFileModel();
        file.setId(UUID.randomUUID().toString());
        file.setFilePath("/transactions/before-1.jpg");
        file.setFileName("before-1.jpg");
        file.setContentType("image/jpeg");
        file.setFileSize(1024L);
        file.setFileType(TransactionFileType.CONDITION_BEFORE);
        file.setCreatedAt(LocalDateTime.now());
        file.setUpdatedAt(LocalDateTime.now());
        return file;
    }

    public static UserSessionModel createSampleSession() {
        UserModel user = createSampleUser();
        UserSessionModel session = new UserSessionModel();
        session.setId(UUID.randomUUID().toString());
        session.setUser(user);
        session.setAccessToken("sample.access.token");
        session.setRefreshToken("sample.refresh.token");
        session.setIsActive(true);
        session.setDeviceInfo("Chrome Browser on Mac");
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
    }
}
