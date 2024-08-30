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
    ClassDB::bind_method("get_safe_insets", &Notch::get_safe_insets);
}

Dictionary Notch::get_safe_insets() {
    if (@available(iOS 11.0, *)) {
         // Iterate over connected scenes to find the active window scene
         for (UIWindowScene *windowScene in UIApplication.sharedApplication.connectedScenes) {
             if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                 // Get the first window from the active scene
                 UIWindow *window = windowScene.windows.firstObject;
                 if (window) {
                     // Get the safe area insets
                     UIEdgeInsets safeInsets = window.safeAreaInsets;

                     // Create a dictionary with all insets as integer values
                     Dictionary insetsDict;
                     
                     insetsDict["top"] =(int)safeInsets.top;
                     insetsDict["bottom"] = (int)safeInsets.bottom;
                     insetsDict["left"] = (int)safeInsets.left;
                     insetsDict["right"] = (int)safeInsets.right;

                     return insetsDict;  // Return the dictionary with all insets
                 }
             }
         }
     }
     
     // Return a dictionary with all zeros if no notch or below iOS 11
    Dictionary emptyInsets;
    
    emptyInsets["top"] = 0;
    emptyInsets["bottom"] = 0;
    emptyInsets["left"] = 0;
    emptyInsets["right"] = 0;

    return emptyInsets;
}

