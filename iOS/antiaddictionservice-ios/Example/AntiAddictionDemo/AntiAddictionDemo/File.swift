//
//  File.swift
//  AntiAddictionDemo
//
//  Created by JiangJiahao on 2021/1/7.
//

import Foundation
import AntiAddictionService


class AntiService {
    func realName() {
        AntiAddictionService.realNameAuth(userToken: "testUserId", name: "name", idCard: "idNumber", phone: "phoneNumber") { identifyState, errorMessage in
            // handle result
        }
        
        AntiAddictionService.checkRealnameState(userToken: "testUserId") { identifyState, userToken, idCardNumber, name in
            // handle result
        } failureHandler: { errorMessage in
            // handle error
        }

    }
}
