//
//  notch.m
//  notch
//
//  Created by Kyoz on 30/08/2024.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

#ifdef VERSION_4_0
#include "core/object/class_db.h"
#else
#include "core/class_db.h"
#endif

#include "notch.h"

Notch *Notch::instance = NULL;

Notch::Notch() {
    instance = this;
    NSLog(@"initialize notch");
}

Notch::~Notch() {
    if (instance == this) {
        instance = NULL;
    }
    NSLog(@"deinitialize notch");
}

Notch *Notch::get_singleton() {
    return instance;
};


void Notch::_bind_methods() {    
    ClassDB::bind_method("get_notch_height", &Notch::get_notch_height);
    ClassDB::bind_method("get_bottom_safe_inset", &Notch::get_bottom_safe_inset);
}

int Notch::get_notch_height() {
    if (@available(iOS 11.0, *)) {
        // Iterate over connected scenes to find the active window scene
        for (UIWindowScene *windowScene in UIApplication.sharedApplication.connectedScenes) {
            if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                // Get the first window from the active scene
                UIWindow *window = windowScene.windows.firstObject;
                if (window) {
                    return (int)window.safeAreaInsets.top; // Return top inset (notch height) as an int
                }
            }
        }
    }
    return 0;  // No notch or below iOS 11
}

int Notch::get_bottom_safe_inset() {
    if (@available(iOS 11.0, *)) {
        // Iterate over connected scenes to find the active window scene
        for (UIWindowScene *windowScene in UIApplication.sharedApplication.connectedScenes) {
            if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                // Get the first window from the active scene
                UIWindow *window = windowScene.windows.firstObject;
                if (window) {
                    return (int)window.safeAreaInsets.bottom; // Return bottom inset as an int
                }
            }
        }
    }
    return 0;  // No bottom inset or below iOS 11
}

