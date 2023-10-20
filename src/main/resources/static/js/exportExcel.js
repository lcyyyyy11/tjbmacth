// document.write("<script type='text/javascript' src='https://cdn.bootcdn.net/ajax/libs/xlsx/0.16.0/jszip.min.js'></script>");
document.write("<script type='text/javascript' src='../../js/jszip.min.js'></script>");
document.write("<script type='text/javascript' src='../../js/xlsx.min.js'></script>");
document.write("<script type='text/javascript' src='../../js/FileSaver.min.js'></script>");


/**
 * 函数功能：将handsOnTable原样导出至excel
 */

/**
 * 导出至excel
 * @param hot handsOnTable实例对象
 * @param fileName 保存的文件名
 * @param startCol 表格起始列
 */
function exportExcel(hot,fileName,startCol,colNum) {
    if (nonNullAndEmpty(hot)) {
        let exportPlugin = hot.getPlugin('MergeCells');
        // let hiddenColNum = hot.getSettings().__proto__.hiddenColumns.columns.length;
        //默认值设置
        if(startCol == undefined || startCol == "" || startCol ==null){
            startCol = 0;
        }
        if(colNum == undefined || colNum == "" || colNum ==null){
            colNum = hot.countCols()-startCol;
        }

        //数据范围
        let endRow = hot.countRows() - 1;
        let endCol = startCol + colNum;

        /*------------------------表头设置-----------------------*/
        //表头信息
        let headersArr = hot.getSettings().__proto__.colHeaders;

        if(headersArr==true){
            //多级表头
            headersArr = hot.getSettings().__proto__.nestedHeaders;
        }else {
            //单级表头
            headersArr = [hot.getSettings().__proto__.colHeaders];
        }
        let headers = changeHeadersArr(headersArr); //转换表头为普通数组
        headers = setHeaderRange(headers,startCol,endCol); //设置表头范围
        let headersMerges = changeHeadersToMerges(headers); //读取表头合并信息
        // replaceMMark(headers,mMark); //去除不合并标识

        // console.log(headers);
        // console.log(headersMerges);

        /*------------------------数据设置-----------------------*/
        let data = hot.getData(0,startCol,endRow, endCol-1); //导出数据范围
        replaceByPatt(data,/<[^>]+>/g,""); //去除html标签
        //数据合并信息
        let dataMerges = exportPlugin.mergedCellsCollection.mergedCells;
        cutDataMerges(dataMerges,startCol);

        let headerRowNum = headers.length;
        let dataRowNum = data.length;
        //总合并信息
        let mergesArr = headersMerges.concat(dataMerges);
        let merges = changeMergeCellsToSheet(mergesArr,headerRowNum);

        // console.log(merges);
        // console.log(mergesArr);

        //列宽、行高信息
        let colWidthArr = getColWidthArr(hot,startCol,endCol);
        let rowHeightArr = getRowHeightArr(hot,headerRowNum,dataRowNum);

        //导出excel
        saveToExcel(headers,data,merges,fileName,colWidthArr,rowHeightArr);
    }
}


/**
 * 导出至excel
 * @param hot handsOnTable实例对象
 * @param fileName 保存的文件名
 * @param startCol 表格起始列
 */
function exportExcel_CJ(hot,fileName,startCol,colNum) {
    if (nonNullAndEmpty(hot)) {
        let exportPlugin = hot.getPlugin('MergeCells');
        // let hiddenColNum = hot.getSettings().__proto__.hiddenColumns.columns.length;
        //默认值设置
        if(startCol == undefined || startCol == "" || startCol ==null){
            startCol = 0;
        }
        if(colNum == undefined || colNum == "" || colNum ==null){
            colNum = hot.countCols()-startCol;
        }

        //数据范围
        let endRow = hot.countRows() - 1;
        let endCol = startCol + colNum;

        /*------------------------表头设置-----------------------*/
        //表头信息
        let headersArr = hot.getSettings().__proto__.colHeaders;
        if(headersArr==true){
            //多级表头
            headersArr = hot.getSettings().__proto__.nestedHeaders;
        }else {
            //单级表头
            headersArr = [hot.getSettings().__proto__.colHeaders];
        }
        // console.log(headersArr)
        let headers = changeHeadersArr(headersArr); //转换表头为普通数组
        headers = setHeaderRange(headers,startCol,endCol); //设置表头范围
        let headersMerges = changeHeadersToMerges_CJ(headers); //读取表头合并信息
        // replaceMMark(headers,mMark); //去除不合并标识

        // console.log(headers);
        // console.log(headersMerges);

        /*------------------------数据设置-----------------------*/
        let data = hot.getData(0,startCol,endRow, endCol-1); //导出数据范围
        replaceByPatt(data,/<[^>]+>/g,""); //去除html标签
        //数据合并信息
        let dataMerges = exportPlugin.mergedCellsCollection.mergedCells;
        cutDataMerges(dataMerges,startCol);

        let headerRowNum = headers.length;
        let dataRowNum = data.length;
        //总合并信息
        let mergesArr = headersMerges.concat(dataMerges);
        let merges = changeMergeCellsToSheet(mergesArr,headerRowNum);

        // console.log(merges);
        // console.log(mergesArr);

        //列宽、行高信息
        let colWidthArr = getColWidthArr(hot,startCol,endCol);
        let rowHeightArr = getRowHeightArr(hot,headerRowNum,dataRowNum);

        //导出excel
        saveToExcel(headers,data,merges,fileName,colWidthArr,rowHeightArr);
    }
}

/**
 * 读取表头信息 并生成合并信息
 * @param headers
 * @returns {[]}
 */
function changeHeadersToMerges_CJ(headers) {
    let headersMerges = []; //用于装合并记录
    let nextCol = 0;  //标记跳过列
    let nextRow = 0;  //标记跳过行
    //合并
    for(let i=0;i<headers.length;i++){
        if(nextRow > 0){
            nextCol = 0;
            //当前行为跳过行
            for(let j=0+nextCol;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                //横向搜索
                for (let k=j+1;k<headers[i].length;k++){
                    if(headers[i][j]==headers[i][k]){
                        colspan += 1;
                        nextCol = colspan;
                    }else {
                        nextCol = colspan;
                        break;
                    }

                }
                //纵向搜索
                for (let k=i+1;k<headers.length;k++){
                    if(headers[i][j]==headers[k][j]){
                        rowspan += 1;
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    //有重复记录
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        //判断是否有重复记录
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }else {
            //当前行常规处理
            for(let j=0;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                for (let k=j+1;k<headers[i].length;k++){
                    if ((i == 0) || (i != 0 && k < 3)) {
                        if(headers[i][j]==headers[i][k]){
                            colspan += 1;
                            nextCol = colspan;
                        }else {
                            nextCol = colspan;
                            break;
                        }
                    }
                }
                for (let k=i+1;k<headers.length;k++) {
                    if (j < 3) {
                        if (headers[i][j] == headers[k][j]) {
                            rowspan += 1;
                        }
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }
        nextRow -= 1;
    }
    return headersMerges;
}

function exportExcel_EG(hot,fileName,startCol,colNum) {
    if (nonNullAndEmpty(hot)) {
        let exportPlugin = hot.getPlugin('MergeCells');
        // let hiddenColNum = hot.getSettings().__proto__.hiddenColumns.columns.length;
        //默认值设置
        if(startCol == undefined || startCol == "" || startCol ==null){
            startCol = 0;
        }
        if(colNum == undefined || colNum == "" || colNum ==null){
            colNum = hot.countCols()-startCol;
        }

        //数据范围
        let endRow = hot.countRows() - 1;
        let endCol = startCol + colNum;

        /*------------------------表头设置-----------------------*/
        //表头信息
        let headersArr = hot.getSettings().__proto__.colHeaders;

        if(headersArr==true){
            //多级表头
            headersArr = hot.getSettings().__proto__.nestedHeaders;
        }else {
            //单级表头
            headersArr = [hot.getSettings().__proto__.colHeaders];
        }

        let headers = changeHeadersArr(headersArr); //转换表头为普通数组
        headers = setHeaderRange(headers,startCol,endCol); //设置表头范围
        let headersMerges = changeHeadersToMerges_EG(headers); //读取表头合并信息
        // replaceMMark(headers,mMark); //去除不合并标识

        // console.log(headers);
        // console.log(headersMerges);

        /*------------------------数据设置-----------------------*/
        let data = hot.getData(0,startCol,endRow, endCol-1); //导出数据范围
        replaceByPatt(data,/<[^>]+>/g,""); //去除html标签
        //数据合并信息
        let dataMerges = exportPlugin.mergedCellsCollection.mergedCells;
        cutDataMerges(dataMerges,startCol);

        let headerRowNum = headers.length;
        let dataRowNum = data.length;
        //总合并信息
        let mergesArr = headersMerges.concat(dataMerges);
        let merges = changeMergeCellsToSheet(mergesArr,headerRowNum);

        // console.log(merges);
        // console.log(mergesArr);

        //列宽、行高信息
        let colWidthArr = getColWidthArr(hot,startCol,endCol);
        let rowHeightArr = getRowHeightArr(hot,headerRowNum,dataRowNum);

        //导出excel
        saveToExcel(headers,data,merges,fileName,colWidthArr,rowHeightArr);
    }
}

function changeHeadersToMerges_EG(headers) {
    let headersMerges = []; //用于装合并记录
    let nextCol = 0;  //标记跳过列
    let nextRow = 0;  //标记跳过行
    //合并
    for(let i=0;i<headers.length;i++){
        if(nextRow > 0){
            nextCol = 0;
            //当前行为跳过行
            for(let j=0+nextCol;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                //横向搜索
                for (let k=j+1;k<headers[i].length;k++){
                    if(headers[i][j]==headers[i][k]){
                        colspan += 1;
                        nextCol = colspan;
                    }else {
                        nextCol = colspan;
                        break;
                    }

                }
                //纵向搜索
                for (let k=i+1;k<headers.length;k++){
                    if(headers[i][j]==headers[k][j]){
                        rowspan += 1;
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    //有重复记录
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        //判断是否有重复记录
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }else {
            //当前行常规处理
            for(let j=0;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                for (let k=j+1;k<headers[i].length;k++){
                    if ((i == 0) || (i != 0 && k < 2)) {
                        if(headers[i][j]==headers[i][k]){
                            colspan += 1;
                            nextCol = colspan;
                        }else {
                            nextCol = colspan;
                            break;
                        }
                    }
                }
                for (let k=i+1;k<headers.length;k++) {
                    if (j < 2) {
                        if (headers[i][j] == headers[k][j]) {
                            rowspan += 1;
                        }
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }
        nextRow -= 1;
    }
    return headersMerges;
}

/**
 * 得到列宽数组
 * @param hot handsOnTable实例对象
 * @param startCol 开始列
 * @param endCol 结束列
 * @returns {[]}
 */
function getColWidthArr(hot,startCol,endCol) {
    let colWidthArr = [];
    for (let i=startCol;i<endCol;i++){
        let colWidth = hot.getColWidth(i);
        colWidthArr.push( {wch: colWidth/7} ); //将像素(px)换算为1/10英寸(excel列宽单位） 约为7.19
    }
    return colWidthArr;
}

/**
 * 得到行高数组
 * @param hot handsOnTable实例对象
 * @param headerRowNum 表头行数
 * @param dataRowNum 数据行数
 * @returns {[]}
 */
function getRowHeightArr(hot,headerRowNum,dataRowNum) {
    let rowHeightArr = [];
    //设置标题行高
    for (let i=0;i<headerRowNum;i++){
        rowHeightArr.push( {hpx: 18} ); //将像素换算为磅(excel行高单位） 为0.75
    }
    //设置其余行高
    for (let i=headerRowNum;i<dataRowNum+headerRowNum;i++){
        let rowHeight = hot.getRowHeight(i);
        rowHeightArr.push( {hpx: rowHeight*0.75} ); //将像素换算为磅(excel行高单位） 为0.75
    }
    return rowHeightArr;
}

/**
 * 根据正则表达式替换数据中的内容
 * @param data 数据
 * @param patt 正则表达式
 * @param text 替换文本
 */
function replaceByPatt(data,patt,text) {
    for (let i=0;i<data.length;i++){
        for (let j=0;j<data[i].length;j++){
            if(data[i][j]==null || data[i][j]==undefined || data[i][j]=="" || typeof data[i][j] != 'string'){
                continue;
            }
            data[i][j] = data[i][j].replace(patt,text);
        }
    }
}
/**
 * 根据开始位置重设合并信息
 * @param dataMerges
 * @param startCol
 */
function cutDataMerges(dataMerges,startCol) {
    dataMerges.forEach(item => item.col -= startCol);
}

/**
 * 将带对象的表头信息转换为正常表头数组
 * @param headers
 */
function changeHeadersArr(headers) {
    let headersArr = [];
    //将对象展开
    for(let i=0;i<headers.length; i++) {
        let row = [];
        for (let j=0;j<headers[i].length; j++) {
            if(typeof headers[i][j] === 'object'){
                let label = headers[i][j].label;
                let span = headers[i][j].colspan;
                for(let k=0;k<span;k++){
                    row.push(label);
                }
            }else {
                row.push(headers[i][j]);
            }
        }
        headersArr.push(row);
    }
    //将空值设置为上下行对应位置的值 为读取合并信息做准备
    for(let i=0;i<headersArr.length; i++) {
        for (let j = 0; j < headersArr[i].length; j++) {
            if(headersArr[i][j]=="") {
                if (i - 1 >= 0) {
                    // if(!headersArr[i-1][j].startWith(mMark)){
                    //     headersArr[i-1][j] = mMark+headersArr[i-1][j];
                    // }
                    // headersArr[i][j] = headersArr[i-1][j];
                    headersArr[i][j] = headersArr[i-1][j];
                } else {
                    // if(!headersArr[i+1][j].startWith(mMark)){
                    //     headersArr[i+1][j] = mMark+headersArr[i+1][j];
                    // }
                    // headersArr[i][j] = headersArr[i+1][j];
                    headersArr[i][j] = headersArr[i+1][j];
                }
            }
        }
    }
    return headersArr;
}

/**
 * 确定表头区域
 * @param headers
 * @param startCol
 * @param endCol
 */
function setHeaderRange(headers,startCol,endCol) {
    let headerArr = [];
    for(i=0;i<headers.length;i++) {
        let row = [];
        for (j=startCol; j<endCol; j++) {
            row.push(headers[i][j]);
        }
        headerArr.push(row);
    }
    return headerArr;
}
/**
 * 去除表头中的不合并标识
 * @param headers
 * @param mMark
 */
function replaceMMark(headers,mMark) {
    for(i=0;i<headers.length;i++) {
        let row = [];
        for (j=0; j<headers[0].length; j++) {
            if(headers[i][j].startWith(mMark)){
                headers[i][j] = headers[i][j].substring(mMark.length);
            }
        }
    }
}
/**
 * 保存至excel文件
 * @param headers 表头
 * @param data 数据
 * @param mergeCells 合并信息
 * @param fileName 文件名
 * @param colWidthArr 列宽信息
 * @param rowHeightArr 行高信息信息
 */
function saveToExcel(headers,data,mergeCells,fileName,colWidthArr,rowHeightArr) {
    // 拼接获得整体数据
    let all = headers.concat(data);
    // console.log("all",all);

    const wopts = {
        bookType: 'xlsx',
        bookSST: true,
        type: 'binary'
    }
    const workBook = {
        SheetNames: ['Sheet1'],
        Sheets: {},
        Props: {}
    }
    workBook.Sheets['Sheet1'] = XLSX.utils.aoa_to_sheet(all); //设置数据
    workBook.Sheets['Sheet1']['!merges'] = mergeCells;  //设置合并信息
    workBook.Sheets['Sheet1']['!cols'] = colWidthArr; //设置列宽
    workBook.Sheets['Sheet1']['!rows'] = rowHeightArr; //设置行高

    //设置样式
    const headerStyle={
        font: {
            name: "宋体",
            sz: "11.25"
        },
        fill: {
            fgColor: {rgb:"f0f0f0"},
            bgColor: {indexed: 64}
        },
        alignment: {
            vertical: "center",
            horizontal: "center",
            wrapText: true
        },
        border: {
            right: {style: "thin", color: {rgb: "CCCCCC"}},
            bottom: {style: "thin", color: {rgb: "CCCCCC"}}
        }
    };
    const dataStyle={
        font: {
            name: "宋体",
            sz: "10.5"
        },
        alignment: {
            vertical: "center",
            horizontal: "bottom",
            wrapText: true
        },
        border: {
            right: {style: "thin", color: {rgb: "CCCCCC"}},
            bottom: {style: "thin", color: {rgb: "CCCCCC"}}
        }
    };
    const endCol = headers[0].length;
    setStyle(workBook.Sheets['Sheet1'],headerStyle,0,headers.length,0,endCol); //设置表头样式
    setStyle(workBook.Sheets['Sheet1'],dataStyle,headers.length,all.length,0,endCol); //设置数据样式
    //利用fileSaver.js保存
    saveAs(new Blob([changeData(XLSX.write(workBook, wopts))],
        { type: 'application/octet-stream' }), fileName + '.xlsx')
}

/**
 * 设置excel样式
 * @param sheet 表对象
 * @param style 样式对象
 * @param startRow 开始行
 * @param endRow 结束行
 * @param startCol 开始列
 * @param endCol 结束列
 */
function setStyle(sheet,style,startRow,endRow,startCol,endCol) {
    for(let i=startRow+1;i<endRow+1;i++){
        for (let j=startCol;j<endCol;j++){
            if(j<=25 && sheet[String.fromCharCode(65+j)+i]!=undefined){
                sheet[String.fromCharCode(65+j)+i].s = style;
            }
            if(j>25 && sheet["A"+String.fromCharCode(39+j)+i]!=undefined){
                sheet["A"+String.fromCharCode(39+j)+i].s = style;
            }
        }
    }
}

/**
 * 读取表头信息 并生成合并信息
 * @param headers
 * @returns {[]}
 */
function changeHeadersToMerges(headers) {
    let headersMerges = []; //用于装合并记录
    let nextCol = 0;  //标记跳过列
    let nextRow = 0;  //标记跳过行
    //合并
    for(let i=0;i<headers.length;i++){
        if(nextRow > 0){
            nextCol = 0;
            //当前行为跳过行
            for(let j=0+nextCol;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                //横向搜索
                for (let k=j+1;k<headers[i].length;k++){
                    if(headers[i][j]==headers[i][k]){
                        colspan += 1;
                        nextCol = colspan;
                    }else {
                        nextCol = colspan;
                        break;
                    }

                }
                //纵向搜索
                for (let k=i+1;k<headers.length;k++){
                    if(headers[i][j]==headers[k][j]){
                        rowspan += 1;
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    //有重复记录
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        //判断是否有重复记录
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }else {
            //当前行常规处理
            for(let j=0;j<headers[i].length;){
                // if(!headers[i][j].startWith(mMark)){
                //     j++;
                //     continue;
                // }
                let colspan = 1;
                let rowspan = 1;
                for (let k=j+1;k<headers[i].length;k++){
                    if(headers[i][j]==headers[i][k]){
                        colspan += 1;
                        nextCol = colspan;
                    }else {
                        nextCol = colspan;
                        break;
                    }
                }
                for (let k=i+1;k<headers.length;k++) {
                    if (headers[i][j] == headers[k][j]) {
                        rowspan += 1;
                    }
                    nextRow = rowspan;
                }
                if(colspan>1 || rowspan>1){
                    let addFlag = true;
                    for (let k = 0; k < headersMerges.length; k++){
                        if(i-headers.length-headersMerges[k].row == headersMerges[k].rowspan-rowspan && headersMerges[k].colspan == colspan && headersMerges[k].col == j){
                            addFlag = false;
                            break;
                        }
                    }
                    if (addFlag){
                        headersMerges.push({row: i - headers.length, col: j, rowspan: rowspan, colspan: colspan, removed: false});
                    }
                }
                j += nextCol;
            }
        }
        nextRow -= 1;
    }
    return headersMerges;
}

/**
 * 将合并信息转化为js-xlsx指定的格式
 * @param mergeCells
 * @param headerRows
 * @returns {[]}
 */
function changeMergeCellsToSheet(mergeCells,headerRows){
    let merges = []
    mergeCells.forEach(item =>{
        if (item.rowspan !==1 || item.colspan !== 1){
            let tmp = {
                s: {//s为开始
                    r: item.row + headerRows,//可以看成开始行,实际是取值范围
                    c: item.col//开始列
                },
                e: {//e结束
                    r: item.row + item.rowspan-1 + headerRows,//结束列
                    c: item.col + item.colspan-1//结束行
                }
            }
            merges.push(tmp)
        }
    })
    return merges;
}

/**
 * 将数据转为二进制流格式
 * @param s
 * @returns {any[]|ArrayBuffer}
 */
function changeData(s) {
    if (typeof ArrayBuffer !== 'undefined') {
        let buf = new ArrayBuffer(s.length)
        let view = new Uint8Array(buf)
        for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xff
        return buf
    } else {
        let buf = new Array(s.length)
        for (let i = 0; i !== s.length; ++i) buf[i] = s.charCodeAt(i) & 0xff
        return buf
    }
}


//------------------------判断是否不为空---------------------------
function nonNullAndEmpty(obj) {
    return (obj != undefined && obj != null && obj != "");
}