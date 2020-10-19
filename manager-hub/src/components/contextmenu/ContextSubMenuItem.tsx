/*
 * MIT License
 *  
 * Copyright (c) 2020 manager
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import {ContextMenuTrigger, MenuItem, SubMenu} from "react-contextmenu";
import React from "react";
import M from "materialize-css";
import styles from "../list/ControlledList.module.css";
import PerfectScrollbar from "react-perfect-scrollbar";
import ScrollBar from "react-perfect-scrollbar";
import DividerContextMenuItem from "./DividerContextMenuItem";

interface Props<T, V> {
    menu: string,
    state: T;
    className?: string;
    header: string,
    emptyMessage: string,
    submenus: V[],
    menuToString: (menu: V) => string,
    onClick: (event: React.MouseEvent, data: { state: T, submenu: V }) => void,
    error?: string | null,
}

export default class ContextSubMenuItem<T, V> extends React.Component<Props<T, V>, {}> {

    public render() {
        const {menu, state, className, header, emptyMessage, submenus, menuToString, onClick, error} = this.props;
        return <>
            <SubMenu title={menu} hoverDelay={250} preventCloseOnClick>
                {error &&
                <MenuItem className={'react-contextmenu-item--disabled red-text'} disabled>{error}</MenuItem>}
                {!error && submenus.length === 0 &&
                <MenuItem className={'react-contextmenu-item--disabled'} disabled>{emptyMessage}</MenuItem>}
                {!error && submenus.length > 0 &&
                <>
                    <MenuItem className={'react-contextmenu-item--disabled'} disabled>{header}</MenuItem>
                    <DividerContextMenuItem/>
                    <DividerContextMenuItem/>
                </>}
                {!error && submenus.map((submenu, index) =>
                    <>
                        <MenuItem key={index} data={{state: state, submenu: submenu}}
                                  onClick={onClick}>{menuToString(submenu)}</MenuItem>
                        {index !== submenus.length - 1 && <DividerContextMenuItem/>}
                    </>
                )}
            </SubMenu>
        </>;
    }
}