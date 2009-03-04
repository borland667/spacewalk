%define oracle_base /opt/apps/oracle

Summary: Oracle configuration files
Name: oracle-config
Version: 1.0.5
Release: 1%{?dist}
License: RHN Subscription License
Group:   RHN/Server
BuildArch: noarch
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source10: oracle.sh
Source11: oracle.csh
Source12: oraenv
Source13: coraenv
Source14: dbhome
Requires(pre): /usr/sbin/useradd
Requires(pre): /usr/sbin/groupadd
Requires(pre): /usr/bin/getent
Requires(pre): shadow-utils
Obsoletes: oracle-instantclient-config <= 10.2.0
Obsoletes: oracle-devel < 10.2.0
Obsoletes: oracle-devel-static < 10.2.0
Obsoletes: oracle-devel-jdbc < 10.2.0

%description
Configuration files for Oracle.

%prep

%build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}

%__install -d -m 755 %{buildroot}%{_sysconfdir}/profile.d
%__install %{SOURCE10} %{SOURCE11} %{buildroot}%{_sysconfdir}/profile.d
%__install -d -m 755 %{buildroot}%{_bindir}
%__install %{SOURCE12} %{SOURCE13} %{SOURCE14} %{buildroot}%{_bindir}
touch %{buildroot}%{_sysconfdir}/tnsnames.ora
touch %{buildroot}%{_sysconfdir}/oratab

%clean
rm -rf %{buildroot}

%pre
# Add the oracle.dba setup
getent group dba >/dev/null    || groupadd -fr dba
getent group oracle >/dev/null || groupadd -fr oracle
getent passwd  oracle >/dev/null || \
        useradd -g oracle -G dba -c "Oracle Server" \
                -r -d %{oracle_base} oracle
exit 0

%files
%defattr(-,root,root)
%attr(-,oracle,dba) %config(noreplace) %{_sysconfdir}/tnsnames.ora
%attr(-,oracle,dba) %config(noreplace) %{_sysconfdir}/oratab
%{_sysconfdir}/profile.d/oracle.sh
%{_sysconfdir}/profile.d/oracle.csh
%{_bindir}/oraenv
%{_bindir}/coraenv
%{_bindir}/dbhome

%changelog
* Wed Mar 04 2009 Michael Mraka <michael.mraka@redhat.com> 1.0.5-1
- updated spec for spacewalk git repo

* Mon Dec  1 2008 Michael Mraka <michael.mraka@redhat.com> 1.0-4
- resolved #470492 - coraenv

* Mon Sep  8 2008 Michael Mraka <michael.mraka@redhat.com> 1.0-3
- added Obsoletes: oracle-devel, oracle-devel-static, oracle-devel-jdbc
- fixed rpmlint warnings

* Wed Jun  4 2008 Michael Mraka <michael.mraka@redhat.com> 1.0-2
- added Obsoletes: oracle-instantclient-config

* Fri May 23 2008 Michael Mraka <michael.mraka@redhat.com> 1.0-1
- config files moved to separate package

