/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.medusa.events;

import eu.hansolo.medusa.Alarm;
import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolbox.evt.type.ChangeEvt;


/**
 * Created by hansolo on 28.01.16.
 */
public class AlarmEvt extends MedusaEvt {
    public static final EvtType<AlarmEvt> ANY   = new EvtType<>(ChangeEvt.ANY, "ANY");
    public static final EvtType<AlarmEvt> ALARM = new EvtType<>(AlarmEvt.ANY, "ALARM");

    private final Alarm alarm;


    // ******************** Constructors **************************************
    public AlarmEvt(final Object src, final EvtType<? extends AlarmEvt> evtType, final Alarm alarm) {
        super(src, evtType);
        this.alarm = alarm;
    }
    public AlarmEvt(final Object src, final EvtType<? extends AlarmEvt> evtType, final EvtPriority priority, final Alarm alarm) {
        super(src, evtType, priority);
        this.alarm = alarm;
    }


    // ******************** Methods *******************************************
    public Alarm getAlarm() { return alarm; }
}
