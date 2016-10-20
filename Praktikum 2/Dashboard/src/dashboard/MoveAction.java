/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

import javax.json.JsonObject;

/**
 *
 * @author joe
 */

interface MoveAction {
        void run();
    
}

interface MoveJobAction {
        void run(printerServer p, JsonObject x);
    
}

interface chosePrinterAction {
        printerServer run();
    
}
