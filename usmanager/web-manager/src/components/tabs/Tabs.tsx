import React, {createRef} from "react";
import {RouteComponentProps} from "react-router";
import M from "materialize-css";
import styles from './Tabs.module.css';
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";

export type Tab = { title: string, id: string, content: () => JSX.Element, disabled?: boolean }

interface TabsProps {
  tabs: Tab[];
}

interface StateToProps {
  sidenav: { user: boolean, width: boolean }
}

type Props = StateToProps & TabsProps & RouteComponentProps;

interface State {
  previousScrollRight: number | null;
  previousScrollLeft: number | null;
  showScrollLeft: boolean;
  showScrollRight: boolean;
}

class Tabs extends React.Component<Props, State> {

  private tabsRef = createRef<HTMLUListElement>();
  private rightScrollTimer: NodeJS.Timeout | null = null;
  private leftScrollTimer: NodeJS.Timeout | null = null;

  state: State = {
    previousScrollLeft: null,
    previousScrollRight: null,
    showScrollLeft: false,
    showScrollRight: false,
  }

  componentDidMount() {
    M.Tabs.init(this.tabsRef.current as Element);
    window.addEventListener('resize', this.handleResize);
    this.setState({
      previousScrollLeft: null,
      previousScrollRight: null,
      showScrollLeft: this.shouldShowScrollLeft(),
      showScrollRight: this.shouldShowScrollRight(),
    })
  }

  componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
    M.Tabs.init(this.tabsRef.current as Element);
    if (prevProps.sidenav !== this.props.sidenav) {
      this.setState({
        showScrollLeft: this.shouldShowScrollLeft(),
        showScrollRight: this.shouldShowScrollRight(),
      })
    }
  }

  componentWillUnmount(): void {
    window.removeEventListener('resize', this.handleResize);
  }

  private handleResize = () => {
    this.setState({
      showScrollLeft: this.shouldShowScrollLeft(),
      showScrollRight: this.shouldShowScrollRight(),
    })
  }

  private shouldShowScrollLeft = (): boolean => {
    const tabs = this.tabsRef.current;
    if (tabs) {
      const show = tabs.scrollWidth > tabs.clientWidth && tabs.scrollLeft > 0;
      this.setState({showScrollLeft: show});
      return show;
    }
    return false;
  }

  private shouldShowScrollRight = (): boolean => {
    const tabs = this.tabsRef.current;
    if (tabs) {
      const show = tabs.scrollWidth > tabs.clientWidth && tabs.scrollLeft + tabs.clientWidth !== tabs.scrollWidth;
      this.setState({showScrollRight: show});
      return show;
    }
    return false;
  }



  private setRightScrollTimer = () => {
    this.stopLeftScrollTimer();
    if (!this.rightScrollTimer) {
      this.rightScrollTimer = setInterval(this.executeRightScroll,25)
    }
  }

  private executeRightScroll = () => {
    const tabs = this.tabsRef.current;
    if (tabs) {
      if (this.state.previousScrollRight === tabs.scrollLeft) {
        this.stopRightScrollTimer();
      }
      this.setState({previousScrollRight: tabs.scrollLeft});
      tabs.scrollTo(tabs.scrollLeft + 10, 0);
      this.shouldShowScrollLeft();
      this.shouldShowScrollRight();
    }
    else {
      this.stopRightScrollTimer();
    }
  }

  private stopRightScrollTimer = () => {
    if (this.rightScrollTimer) {
      clearInterval(this.rightScrollTimer);
    }
    this.rightScrollTimer = null;
  }

  private executeLeftScroll = () => {
    const tabs = this.tabsRef.current;
    if (tabs) {
      if (this.state.previousScrollLeft === tabs.scrollLeft) {
        this.stopLeftScrollTimer();
      }
      this.setState({previousScrollLeft: tabs.scrollLeft});
      tabs.scrollTo(tabs.scrollLeft - 10, 0);
      this.shouldShowScrollLeft();
      this.shouldShowScrollRight();
    }
    else {
      this.stopLeftScrollTimer();
    }
  }

  private setLeftScrollTimer = () => {

    this.stopRightScrollTimer();
    this.leftScrollTimer = setInterval(this.executeLeftScroll,25)
  }

  private stopLeftScrollTimer = () => {
    if (this.leftScrollTimer) {
      clearInterval(this.leftScrollTimer)
    }
    this.leftScrollTimer = null;
  }

  /*private scrollbarShowing = () => {
    const body = document.body, html = document.documentElement;
    const height = Math.max( body.scrollHeight, body.offsetHeight,
      html.clientHeight, html.scrollHeight, html.offsetHeight );
    const clientHeight = window.innerHeight ? window.innerHeight : body.clientHeight;
    return height > clientHeight;
  }*/

  public render() {
    const {tabs} = this.props;
    return (
      <div>
        {this.state.showScrollLeft &&
        <button className={`btn-flat btn-small white-text left ${styles.scrollButton} ${styles.leftScrollButton}`}
                onTouchStart={this.setLeftScrollTimer}
                onMouseDown={this.setLeftScrollTimer}
                onTouchEnd={this.stopLeftScrollTimer}
                onMouseUp={this.stopLeftScrollTimer}>
          <i className="material-icons">arrow_back</i>
        </button>}
        {this.state.showScrollRight &&
        <button className={`btn-flat btn-small white-text right ${styles.scrollButton} ${styles.rightScrollButton}`}
                onTouchStart={this.setRightScrollTimer}
                onMouseDown={this.setRightScrollTimer}
                onTouchEnd={this.stopRightScrollTimer}
                onMouseUp={this.stopRightScrollTimer}>
          <i className="material-icons">arrow_forward</i>
        </button>}
        <div className="row">
          <div className="col s12">
            <ul className="tabs tabs-fixed-width" ref={this.tabsRef}>
              {tabs.map((tab, index) =>
                <li key={index} className={`tab ${tab.disabled ? 'disabled' : ''}`}>
                  <a href={tabs.length === 1 ? undefined : `#${tab.id}`}>{tab.title}</a>
                </li>
              )}
            </ul>
            {tabs.map((tab, index) =>
              <div id={tab.id} key={index} className={`tab-content ${styles.tabContent} col s12`}>
                {tab.content()}
              </div>
            )}
          </div>
        </div>
      </div>

    )
  }

}

function mapStateToProps(state: ReduxState): StateToProps {
  return {
    sidenav: state.ui.sidenav,
  }
}

export default connect(mapStateToProps)(Tabs);