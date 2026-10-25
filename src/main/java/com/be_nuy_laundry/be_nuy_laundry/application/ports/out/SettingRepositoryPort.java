package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;

import java.util.Optional;

public interface SettingRepositoryPort {

    SettingModel save(SettingModel model);

    Optional<SettingModel> getSetting();

}
