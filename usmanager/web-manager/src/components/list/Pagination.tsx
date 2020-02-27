import * as React from "react";
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";
import {PageNumber} from "./PageNumber";

interface StateToProps {
  sidenavVisible: boolean;
}

interface PaginationProps {
  max: number;
  page: number;
  setPage: (pageIndex: number) => void;
  prevPage: () => void;
  nextPage: () => void;
}

type Props = StateToProps & PaginationProps;

class Pagination extends React.Component<Props, {}> {

  private noEllipsis = (max: number) =>
    Array.from({length: max + 1}, (x, i) => i + 1);

  private beforeEllipsis = (max: number, page: number): number[] =>
    Array.from({length: page < 3 ? 3 : 1}, (x, i) => i + 1);

  private afterEllipsis = (max: number, page: number): number[] =>
    Array.from({length: page > max - 3 ? 3 : 1}, (x, i) => max - i + 1).reverse();

  private betweenEllipsis = (max: number, page: number): number[] =>
    Array.from({length: 1}, (x, i) => page + 1).reverse();

  render() {
    const {max, page, setPage, prevPage, nextPage, sidenavVisible} = this.props;
    const needsEllipsis = max >= 10;
    let pagination;
    if (!needsEllipsis) {
      const noEllipsis: number[] = this.noEllipsis(max);
      pagination = noEllipsis.map((pageNumber, index) =>
        <PageNumber key={index} page={pageNumber} active={index === page} setPage={setPage}/>
      )
    } else {
      const beforeEllipsis: number[] = this.beforeEllipsis(max, page);
      const betweenEllipsis: number[] = this.betweenEllipsis(max, page);
      const afterEllipsis: number[] = this.afterEllipsis(max, page);
      pagination =
        <>
          {beforeEllipsis.map((pageNumber, index) =>
            <PageNumber key={index} page={pageNumber} active={index === page} setPage={setPage}/>
          )}
          {beforeEllipsis.length == 1 && (
            <>
              <i className="material-icons white-text bottom" style={{margin: '7px 7px 0 7px'}}>more_horiz</i>
              {afterEllipsis.length == 1 && (
                betweenEllipsis.map((pageNumber, index) =>
                  <PageNumber key={index} page={pageNumber} active={true}
                              setPage={setPage}/>
                )
              )}
            </>
          )}
          {afterEllipsis.length == 1 && (
            <i className="material-icons white-text bottom" style={{margin: '7px 7px 0 7px'}}>more_horiz</i>
          )}
          {afterEllipsis.map((pageNumber, index) =>
            <PageNumber key={index} page={pageNumber} active={max - (afterEllipsis.length - index) + 1 === page}
                        setPage={setPage}/>
          )}
        </>
    }
    return (
      <ul className='pagination no-select'
          style={max < 1 ? { visibility: "hidden" } : (sidenavVisible ? {left: "100px"} : undefined)}>
        <li className={page === 0 ? "disabled" : undefined}>
          <a onClick={prevPage}>
            <i className="material-icons small">chevron_left</i>
          </a>
        </li>
        {pagination}
        <li className={page === this.props.max ? "disabled" : undefined}>
          <a onClick={nextPage}>
            <i className="material-icons small">chevron_right</i>
          </a>
        </li>
      </ul>
    )
  }
}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
  }
);

export default connect(mapStateToProps)(Pagination);
