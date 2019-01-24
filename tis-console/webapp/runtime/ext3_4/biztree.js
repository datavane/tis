Ext.ns('Ext.taobao');
Ext.taobao.AppsLoader =Ext.extend(Ext.tree.TreeLoader, {
  processResponse : function(response, node, callback, scope){
        var json = response.responseText;
        try {
            var o = response.responseData || Ext.decode(json);
            node.beginUpdate();
            for(var i = 0, len = o.list.length; i < len; i++){
                var n = this.createNode(o.list[i]);
                if(n){
                    node.appendChild(n);
                }
            }
            node.endUpdate();
            this.runCallback(callback, scope || node, [node]);
        }catch(e){
            this.handleFailure(response);
        }
    }
});