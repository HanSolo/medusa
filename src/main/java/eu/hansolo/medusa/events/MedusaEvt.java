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

import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolbox.evt.type.ChangeEvt;
import eu.hansolo.toolbox.evt.type.PropertyChangeEvt;


public class MedusaEvt extends ChangeEvt {
    public static final EvtType<MedusaEvt> ANY           = new EvtType<>(ChangeEvt.ANY, "ANY");
    public static final EvtType<MedusaEvt> RECALC        = new EvtType<>(MedusaEvt.ANY, "RECALC");
    public static final EvtType<MedusaEvt> REDRAW        = new EvtType<>(MedusaEvt.ANY, "REDRAW");
    public static final EvtType<MedusaEvt> RESIZE        = new EvtType<>(MedusaEvt.ANY, "RESIZE");
    public static final EvtType<MedusaEvt> LED           = new EvtType<>(MedusaEvt.ANY, "LED");
    public static final EvtType<MedusaEvt> LCD           = new EvtType<>(MedusaEvt.ANY, "LCD");
    public static final EvtType<MedusaEvt> VISIBILITY    = new EvtType<>(MedusaEvt.ANY, "VISIBILITY");
    public static final EvtType<MedusaEvt> INTERACTIVITY = new EvtType<>(MedusaEvt.ANY, "INTERACTIVITY");
    public static final EvtType<MedusaEvt> FINISHED      = new EvtType<>(MedusaEvt.ANY, "FINISHED");
    public static final EvtType<MedusaEvt> SECTION       = new EvtType<>(MedusaEvt.ANY, "SECTION");
    public static final EvtType<MedusaEvt> ALERT         = new EvtType<>(MedusaEvt.ANY, "ALERT");
    public static final EvtType<MedusaEvt> VALUE         = new EvtType<>(MedusaEvt.ANY, "VALUE");


    // ******************** Constructors **************************************
    public MedusaEvt(final Object src, final EvtType<? extends MedusaEvt> evtType) {
        super(src, evtType);
    }
    public MedusaEvt(final Object src, final EvtType<? extends MedusaEvt> evtType, final EvtPriority priority) {
        super(src, evtType, priority);
    }


    // ******************** Methods *******************************************
    public EvtType<? extends MedusaEvt> getEvtType() { return (EvtType<? extends MedusaEvt>) super.getEvtType(); }
}
