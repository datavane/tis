<?xml version="1.0" encoding="UTF-8"?>
<schema name="template" version="1.5">
 <types>
     <fieldType name="string"  class="solr.StrField"  sortMissingLast="true" omitNorms="true" omitTermFreqAndPositions="true"/>
     <fieldType name="double"  class="solr.TrieDoubleField" precisionStep="0" positionIncrementGap="0"/>
     <fieldType name="tdouble" class="solr.TrieDoubleField" precisionStep="8" positionIncrementGap="0"/>
    
    <fieldType name="float"    class="solr.TrieFloatField"  precisionStep="0" positionIncrementGap="0"/>
    <fieldType name="tfloat"   class="solr.TrieFloatField"  precisionStep="8" positionIncrementGap="0"/>
    
    <fieldType name="int"      class="solr.TrieIntField"    precisionStep="0" positionIncrementGap="0"/>
   
    <fieldType name="tint"     class="solr.TrieIntField"    precisionStep="8" positionIncrementGap="0"/>
    
    <fieldType  name="long"    class="solr.TrieLongField"   precisionStep="0" positionIncrementGap="0"/>
    <fieldType name="tlong"    class="solr.TrieLongField"   precisionStep="8" positionIncrementGap="0"/>
  
 </types> 
 <fields>
    <field name="id"        type="string" stored="true" indexed="true" required="true"/>
    <field name="entity_id" type="string" stored="true" indexed="true" required="true"  docValues="true"/>
    <field name="shop_kind" type="int"    stored="true" indexed="true" required="false"/>
  
  

    <field name="_version_"  type="long"   indexed="false" stored="true" required="true" docValues="true"/>
    <field name="text"       type="string" indexed="true"  stored="false" multiValued="true"/>

 
 </fields>
 <sharedKey>entity_id</sharedKey>
 <uniqueKey>entity_id</uniqueKey>
  
</schema>