//
//  notch_module.m
//  notch
//
//  Created by Kyoz on 10/07/2023.
//


#ifdef VERSION_4_0
#include "core/config/engine.h"
#else
#include "core/engine.h"
#endif


#include "notch_module.h"

Notch * notch;

void register_notch_types() {
    notch = memnew(Notch);
    Engine::get_singleton()->add_singleton(Engine::Singleton("Notch", notch));
};

void unregister_notch_types() {
    if (notch) {
        memdelete(notch);
    }
}
