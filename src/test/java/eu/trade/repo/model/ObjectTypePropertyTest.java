package eu.trade.repo.model;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.junit.Test;

public class ObjectTypePropertyTest {
	
	
	private static ObjectTypeProperty build() {
		ObjectTypeProperty otp = new ObjectTypeProperty();
		otp.setPropertyType(PropertyType.BOOLEAN);
		otp.setCardinality(Cardinality.SINGLE);
		otp.setUpdatability(Updatability.ONCREATE);
		
		return otp;
	}
	
	@Test
	public void testComparator01() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator02() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
	
		o1.setId(123);
		o2.setId(123);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator03() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		Repository r1 = new Repository();
		r1.setCmisId("repo");
		ObjectType ot1 = new ObjectType("cmis:document");
		ot1.setRepository(r1);
		o1.setObjectType(ot1);
		
		Repository r2 = new Repository();
		r2.setCmisId("repo");
		ObjectType ot2 = new ObjectType("cmis:document");
		ot2.setRepository(r2);
		o2.setObjectType(ot2);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator04() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();

		o1.setCmisId("cmisId");
		o2.setCmisId("cmisId");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator05() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();

		o1.setLocalName("ln");
		o2.setLocalName("ln");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator06() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setLocalNamespace("lns");
		o2.setLocalNamespace("lns");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator07() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setQueryName("qn");
		o2.setQueryName("qn");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator08() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDisplayName("dn");
		o2.setDisplayName("dn");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator09() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDescription("d");
		o2.setDescription("d");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator10() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setRequired(true);
		o2.setRequired(true);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator11() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setQueryable(true);
		o2.setQueryable(true);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator12() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setOrderable(true);
		o2.setOrderable(true);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator13() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setChoices("A;B;C");
		o2.setChoices("A;B;C");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator14() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setOpenChoice(true);
		o2.setOpenChoice(true);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator15() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDefaultValue("def");
		o2.setDefaultValue("def");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator16() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMinValue(123);
		o2.setMinValue(123);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator17() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMaxValue(456);
		o2.setMaxValue(456);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator18() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setResolution(DateTimeResolution.DATE);
		o2.setResolution(DateTimeResolution.DATE);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator19() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		
		o1.setPrecision(DecimalPrecision.BITS32);
		o2.setPrecision(DecimalPrecision.BITS32);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}
	
	@Test
	public void testComparator20() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMaxLength(789);
		o2.setMaxLength(789);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 0);
	}


	@Test
	public void testComparator21() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
	
		o1.setId(123);
		o2.setId(1230);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator22() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		Repository r1 = new Repository();
		r1.setCmisId("repo");
		ObjectType ot1 = new ObjectType("cmis:document");
		ot1.setRepository(r1);
		o1.setObjectType(ot1);
		
		Repository r2 = new Repository();
		r2.setCmisId("repo");
		ObjectType ot2 = new ObjectType("cmis:folder");
		ot2.setRepository(r2);
		o2.setObjectType(ot2);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator23() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();

		o1.setCmisId("cmisId1");
		o2.setCmisId("cmisId2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator24() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();

		o1.setLocalName("ln1");
		o2.setLocalName("ln2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator25() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setLocalNamespace("lns1");
		o2.setLocalNamespace("lns2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator26() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setQueryName("qn1");
		o2.setQueryName("qn2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator27() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDisplayName("dn1");
		o2.setDisplayName("dn2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator28() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDescription("d1");
		o2.setDescription("d2");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator29() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setRequired(true);
		o2.setRequired(false);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator30() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setQueryable(true);
		o2.setQueryable(false);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator31() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setOrderable(true);
		o2.setOrderable(false);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator32() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setChoices("A;B;C");
		o2.setChoices("A;B;C;D");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator33() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setOpenChoice(true);
		o2.setOpenChoice(false);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator34() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setDefaultValue("def");
		o2.setDefaultValue("defg");
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator35() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMinValue(123);
		o2.setMinValue(1234);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator36() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMaxValue(456);
		o2.setMaxValue(4567);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator37() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setResolution(DateTimeResolution.DATE);
		o2.setResolution(DateTimeResolution.TIME);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator38() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		
		o1.setPrecision(DecimalPrecision.BITS32);
		o2.setPrecision(DecimalPrecision.BITS64);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
	
	@Test
	public void testComparator39() {
		ObjectTypeProperty o1 = build();
		ObjectTypeProperty o2 = build();
		
		o1.setMaxLength(789);
		o2.setMaxLength(7890);
		Assert.assertEquals(ObjectTypeProperty.FULL_COMPARATOR.compare(o1, o2), 1);
	}
}
