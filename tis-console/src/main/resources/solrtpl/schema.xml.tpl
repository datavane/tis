<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE schema SYSTEM  "solrres://tisrepository/dtd/solrschema.dtd">
<schema name="template" version="1.5">
 <types>
    <fieldType name="string"  class="solr.StrField"  sortMissingLast="true" omitNorms="true" omitTermFreqAndPositions="true"/>
    
    <fieldType name="pint"    class="solr.IntPointField"       docValues="false"/>
    <fieldType name="pfloat"  class="solr.FloatPointField"     docValues="false"/>
    <fieldType name="plong"   class="solr.LongPointField"      docValues="false"/>
    <fieldType name="pdouble" class="solr.DoublePointField"    docValues="false"/>
  
  
    <fieldType name="bitwise" class="com.qlangtech.tis.solrextend.fieldtype.BitwiseField" 
               omitNorms="true"  omitTermFreqAndPositions="true" />
               
    <fieldType name="cn_ik" class="solr.TextField"  
       autoGeneratePhraseQueries="false"  omitNorms="true" omitPositions="true">  
      <analyzer  type="index">
        <tokenizer class="solr.PatternTokenizerFactory" pattern=",\s*" />
       <filter class="solr.NGramFilterFactory"  minGramSize="1" maxGramSize="7" />
       <filter class="solr.StandardFilterFactory"/>
       <filter class="solr.TrimFilterFactory"/>
     </analyzer> 
     <analyzer type="query" class="com.qlangtech.tis.solrextend.analyzer.IKAnalyzer" >
     </analyzer>
   </fieldType>
   
   <fieldType name="comma_split" class="solr.TextField" positionIncrementGap="100" 
       autoGeneratePhraseQueries="false"  omitNorms="true" omitPositions="true">  
    <analyzer  type="index">
       <tokenizer class="solr.PatternTokenizerFactory" pattern=",\s*" />
       <filter class="solr.LowerCaseFilterFactory"/>
    </analyzer> 
      <analyzer  type="query">
       <tokenizer class="solr.PatternTokenizerFactory" pattern=",\s*" />
       <filter class="solr.LowerCaseFilterFactory"/>
    </analyzer>
   </fieldType>
   
   <fieldType name="cn_pinyin" class="solr.TextField" positionIncrementGap="100" 
       autoGeneratePhraseQueries="false"  omitNorms="true" omitPositions="true">  
    <analyzer  type="index">
       <tokenizer class="com.qlangtech.tis.solrextend.fieldtype.WhitespaceRemoveTokenizerFactory"  />
        <filter class="com.qlangtech.tis.solrextend.fieldtype.pinyin.AllWithNGramTokenFactory" minGramSize="1" maxGramSize="7"/>
       <filter class="solr.StandardFilterFactory"/>
       <filter class="solr.TrimFilterFactory"/>
       <filter class="solr.LowerCaseFilterFactory"/>
       <filter class="com.qlangtech.tis.solrextend.fieldtype.pinyin.PinyinTokenFilterFactory" />   
    </analyzer> 
     <analyzer  type="query">
       <tokenizer class="solr.PatternTokenizerFactory" pattern=",\s*" />
       <filter class="solr.LowerCaseFilterFactory"/>
    </analyzer>
  </fieldType>
   
 </types> 
 <fields>
    <field name="id"        type="string"  stored="true" indexed="true" required="true"/>
    <field name="name"      type="string"  stored="true" indexed="true" required="true"  docValues="true"/>
    <field name="kind"      type="pint"    stored="true" indexed="true" required="false"/>
  
  

    <field name="_version_"  type="plong"   indexed="false" stored="true" required="true" docValues="true"/>
    <field name="text"       type="string" indexed="true"  stored="false" multiValued="true"/>

 </fields>
 <sharedKey>kind</sharedKey>
 <uniqueKey>kind</uniqueKey>
  
</schema>