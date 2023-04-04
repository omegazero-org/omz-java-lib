
BINDIR := bin

# https://stackoverflow.com/a/18258352
rwildcard = $(foreach d,$(wildcard $(1:=/*)),$(call rwildcard,$d,$2) $(filter $(subst *,%,$2),$d))

JAVA_CP := json-20210307.jar
JAVAC_FLAGS := -Werror -Xlint:all,-processing
JAVA_PATH_SEPARATOR := $(strip $(shell java -XshowSettings:properties 2>&1 | grep path.separator | cut -d '=' -f2))


.PHONY: all
all: config events logging util alljar

.PHONY: config
config: $(BINDIR)/config.jar
.PHONY: events
events: $(BINDIR)/events.jar
.PHONY: logging
logging: $(BINDIR)/logging.jar
.PHONY: util
util: $(BINDIR)/util.jar
.PHONY: omz-java-lib-all
alljar: $(BINDIR)/omz-java-lib-all.jar

.PHONY: clean
clean:
	rm -r $(BINDIR)/*

define pre_build
	@mkdir -p $(BINDIR)/$(1)
endef

define post_build
	@[ ! -d $(1)/main/resources ] || cp -r $(1)/main/resources/* $(BINDIR)/$(1)
	jar cf $(BINDIR)/$(1).jar -C $(BINDIR)/$(1) .
endef

$(BINDIR)/config.jar: $(call rwildcard,config/main/java,*.java)
	$(call pre_build,config)
	javac $(JAVAC_FLAGS) -d $(BINDIR)/config -cp "$(JAVA_CP)" $(filter %.java,$^)
	$(call post_build,config)

$(BINDIR)/events.jar: $(BINDIR)/util.jar $(call rwildcard,events/main/java,*.java)
	$(call pre_build,events)
	javac $(JAVAC_FLAGS) -d $(BINDIR)/events -cp "$(JAVA_CP)$(JAVA_PATH_SEPARATOR)$(BINDIR)/util.jar" $(filter %.java,$^)
	$(call post_build,events)

$(BINDIR)/logging.jar: $(BINDIR)/util.jar $(BINDIR)/events.jar $(call rwildcard,logging/main/java,*.java)
	$(call pre_build,logging)
	javac $(JAVAC_FLAGS) -d $(BINDIR)/logging -cp "$(JAVA_CP)$(JAVA_PATH_SEPARATOR)$(BINDIR)/util.jar$(JAVA_PATH_SEPARATOR)$(BINDIR)/events.jar" $(filter %.java,$^)
	$(call post_build,logging)

$(BINDIR)/util.jar: $(call rwildcard,util/main/java,*.java)
	$(call pre_build,util)
	javac $(JAVAC_FLAGS) -d $(BINDIR)/util -cp "$(JAVA_CP)" $(filter %.java,$^)
	$(call post_build,util)

$(BINDIR)/omz-java-lib-all.jar: $(BINDIR)/config.jar $(BINDIR)/events.jar $(BINDIR)/logging.jar $(BINDIR)/util.jar $(call rwildcard,misc/main/java,*.java)
	$(call pre_build,omz-java-lib-all)
	javac $(JAVAC_FLAGS) -d $(BINDIR)/omz-java-lib-all -cp "$(JAVA_CP)$(JAVA_PATH_SEPARATOR)$(BINDIR)/config.jar$(JAVA_PATH_SEPARATOR)$(BINDIR)/events.jar$(JAVA_PATH_SEPARATOR)\
$(BINDIR)/logging.jar$(JAVA_PATH_SEPARATOR)$(BINDIR)/util.jar" $(filter %.java,$^)
	cp -r $(BINDIR)/util/* $(BINDIR)/config/* $(BINDIR)/events/* $(BINDIR)/logging/* $(BINDIR)/omz-java-lib-all/
	$(call post_build,omz-java-lib-all)
