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

import React, {createRef} from "react";
import ScrollBar from "react-perfect-scrollbar";
import {Link} from "react-router-dom";
import CardTitle from "../list/CardTitle";
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";
import {ContextMenu, ContextMenuTrigger, MenuItem, MenuItemProps} from "react-contextmenu";
import ConfirmDialog from "../dialogs/ConfirmDialog";
import {deleteData} from "../../utils/api";
import {RestOperation} from "../form/Form";
import ActionProgressBar from "../actionloading/ActionProgressBar";

interface CardProps<T> {
    id?: string; // TODO remove optional
    title?: string;
    link?: { to: { pathname: string, state: T } };
    height?: number | string;
    margin?: number | string;
    hoverable?: boolean;
    children?: any[];
    delete?: RestOperation; // TODO remove optional
    loading?: boolean; // TODO remove optional
    contextMenuItems?: JSX.Element[];
}

interface State {
    loading: boolean;
}

interface StateToProps {
    sidenavVisible: boolean;
}

type Props<T> = CardProps<T> & StateToProps;

class GenericCard<T> extends React.Component<Props<T>, State> {

    private CARD_ITEM_HEIGHT = 39;

    private scrollbar: (ScrollBar | null) = null;
    private card = createRef<HTMLDivElement>();
    private cardContent = createRef<HTMLDivElement>();

    constructor(props: Props<T>) {
        super(props);
        this.state = {
            loading: props.loading || false
        }
    }

    public componentDidMount(): void {
        this.scrollbar?.updateScroll();
        this.blockBodyScroll();
    }

    componentDidUpdate(prevProps: Readonly<Props<T>>, prevState: Readonly<{}>, snapshot?: any) {
        if (prevProps.sidenavVisible !== this.props.sidenavVisible) {
            this.scrollbar?.updateScroll();
        }
    }

    public render() {
        const {id, title, link, margin, contextMenuItems} = this.props;
        const {loading} = this.state;
        const cardElement =
            <>
                <ActionProgressBar loading={loading} backgroundColor={"#1F1F1F"} progressBarColor={"#c93030"}/>
                {this.cardElement()}
            </>;
        return (
            <>
                <ContextMenuTrigger id={`contextMenu-${id}`}>
                    <div className={`col s6 m4 l3`} style={{margin}}>
                        {link
                            ? <Link to={{
                                pathname: link?.to.pathname,
                                state: link?.to.state
                            }}>
                                {cardElement}
                            </Link>
                            : {cardElement}}
                    </div>
                </ContextMenuTrigger>
                <ContextMenu id={`contextMenu-${id}`} className="custom-context-menu">
                    {link &&
                    <>
                        <Link to={{
                            pathname: link?.to.pathname,
                            state: link?.to.state
                        }}>
                            <MenuItem className='custom-context-menu-item'>
                                Details
                            </MenuItem>
                        </Link>
                        <MenuItem className="custom-context-menu-item-divider"/>
                    </>}
                    {contextMenuItems?.map((menuItem, index) =>
                        <div key={index}>
                            {menuItem}
                            <MenuItem className="custom-context-menu-item-divider"/>
                        </div>
                    )}
                    <div className={`${loading ? '' : 'modal-trigger'}`} data-target={id}>
                        <MenuItem className={`${loading ? 'custom-context-menu-item-disable' : 'custom-context-menu-item'} red-text`}
                                  data={{foo: 'bar'}}
                                  disabled={loading}>
                            Delete
                        </MenuItem>
                    </div>
                </ContextMenu>
                <ConfirmDialog id={id || ''}
                               message={`delete ${title}`}
                               confirmCallback={this.onDelete}/>
            </>
        )
    }

    private onDelete = () => {
        const args = this.props.link?.to.state;
        deleteData(this.props.delete?.url || '',
            () => this.props.delete?.successCallback(args),
            (reply) => this.props.delete?.failureCallback(reply, args));
        this.setState({loading: true});
    }

    private getChildrenCount = (): number =>
        React.Children.count(this.props.children);

    private getHeight = (): number => {
        let height = this.props.height || this.getChildrenCount() * this.CARD_ITEM_HEIGHT;
        if (typeof height == 'string') {
            height = Number(height.replace(/[^0-9]/g, ''));
        }
        return height;
    };

    private blockBodyScroll = () => {
        const cardContent = this.cardContent.current;
        if (cardContent && cardContent.scrollHeight > this.getHeight()) {
            this.card.current?.addEventListener('wheel', event => event.preventDefault())
        }
    };

    private cardElement = (): JSX.Element => {
        const {title, hoverable, children} = this.props;
        const childrenCount = this.getChildrenCount();
        return (
            <div className={hoverable ? 'hoverable' : undefined}
                 style={childrenCount === 0 ? {borderBottom: '1px black solid'} : undefined}>
                {title && <CardTitle title={title}/>}
                {childrenCount > 0 && (
                    <div className={`card gridCard`}
                         style={{height: this.getHeight()}}
                         ref={this.card}>
                        <ScrollBar ref={(ref) => {
                            this.scrollbar = ref;
                        }}
                                   component="div">
                            <div className='card-content' ref={this.cardContent}>
                                {children}
                            </div>
                        </ScrollBar>
                    </div>)}
            </div>
        )
    };

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
    }
);

export default function Card<T>() {
    return connect(mapStateToProps)(GenericCard as new(props: Props<T>) => GenericCard<T>);
}
