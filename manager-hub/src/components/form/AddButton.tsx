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

import {Link} from "react-router-dom";
import React, {createRef} from "react";
import styles from "./AddButton.module.css";
import M from "materialize-css";
import ScrollBar from "react-perfect-scrollbar";

interface Props {
    button: { text: string, tooltip?: string };
    pathname?: string;
    dropdown?: {
        id: string | number,
        title: string,
        empty?: string,
        data: { text: string, pathname: string }[]
    }
}

export default class AddButton extends React.Component<Props, {}> {

    private dropdown = createRef<HTMLDivElement>();
    private scrollbar: (ScrollBar | null) = null;

    public componentDidMount(): void {
        this.initDropdown();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.initDropdown();
    }

    public render() {
        const {button, pathname, dropdown} = this.props;
        return (
            <>
                {!dropdown
                    ? <Link className={`btn-flat btn-small green-text ${styles.button}`}
                            to={pathname || ""}>
                        {button.text}
                    </Link>
                    : <>
                        <div
                            className={`btn-flat btn-small green-text ${styles.button} dropdown-trigger`}
                            data-target={`dropdown-${dropdown.id}`}
                            ref={this.dropdown}>
                            {button.text}
                        </div>
                        <ul id={`dropdown-${dropdown.id}`}
                            className={`dropdown-content ${styles.dropdown}`}>
                            <li className={`${styles.disabled}`}>
                                <a className={`${!dropdown?.data.length ? styles.dropdownEmpty : undefined}`}>
                                    {dropdown.data.length ? dropdown.title : dropdown.empty}
                                </a>
                            </li>
                            <ScrollBar ref={(ref) => {
                                this.scrollbar = ref;
                            }}>
                                {dropdown.data.map((data, index) =>
                                    <li key={index}>
                                        <Link to={data.pathname}>
                                            {data.text}
                                        </Link>
                                    </li>
                                )}
                            </ScrollBar>
                        </ul>
                    </>
                }

            </>
        );
    }

    private initDropdown = () => {
        M.Dropdown.init(this.dropdown.current as Element,
            {
                onOpenEnd: this.onOpenDropdown
            });
    };

    private onOpenDropdown = () =>
        this.scrollbar?.updateScroll();

}
